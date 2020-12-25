package com.robkonarski.workflow.engine

import com.robkonarski.workflow.*
import com.robkonarski.workflow.core.debugLog
import com.robkonarski.workflow.core.query
import com.robkonarski.workflow.core.tryIgnore
import com.robkonarski.workflow.db.Schema
import com.robkonarski.workflow.db.connectDatabase
import com.robkonarski.workflow.migration.MigrationService
import com.robkonarski.workflow.migration.MigrationsRepository
import com.zaxxer.hikari.HikariConfig
import org.jetbrains.exposed.sql.SqlLogger
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

internal class WorkflowEngineImpl @JvmOverloads constructor(
    databaseConfig: DatabaseConfig,
    registeredActivities: List<Activity<*, *>>,
    private val engineConfig: EngineConfig = EngineConfig(),
    private val startWorker: (() -> Unit) -> Unit = { Thread { it() }.start() },
    private val await: ((delay: Long) -> Unit) = { Thread.sleep(it) },
    private val engineLogger: (message: String) -> Unit = debugLog,
    databaseLogger: SqlLogger? = null,
    additionalHikariConfig: HikariConfig.() -> Unit = {},
    private val table: Schema.Workflows = Schema.Workflows(databaseConfig.tablePrefix),
    private val repo: WorkflowRepository = WorkflowRepositoryImpl(engineConfig.workers, table),
) : WorkflowEngine {

    private val migrationsTable = Schema.Migrations(databaseConfig.tablePrefix)
    private val migrationsRepo: MigrationsRepository = MigrationsRepository(migrationsTable)

    private val activities = registeredActivities.map { activity ->
        activity.uniqueId() to activity
    }.toMap()

    private val resolveActivityTimeout: (activity: String) -> Int =
        { activity -> activities[activity]?.timeoutSeconds() ?: 60 }

    private val running = AtomicBoolean(false)

    init {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        connectDatabase(
            config = databaseConfig,
            desiredParallelism = engineConfig.workers,
            schemas = listOf(table, migrationsTable),
            migrationService = MigrationService(migrationsRepo, engineLogger),
            logger = databaseLogger,
            additionalHikariConfig = additionalHikariConfig
        )

        startWorker {
            while (true) {
                if (!running.get()) continue
                tryIgnore { babysit() }
            }
        }

        (0 until engineConfig.workers).forEach {
            startWorker {
                while (true) {
                    if (!running.get()) continue
                    tryIgnore { work() }
                }
            }
        }
    }

    override fun start() = running.set(true)

    override fun stop() = running.set(false)

    override fun createWorkflow(workflow: Workflow): UUID? {
        if (!activities.keys.containsAll(workflow.activities)) {
            throw IllegalArgumentException("can't start a workflow with unknown activities")
        }

        return query { repo.create(workflow) }
    }

    override fun createWorkflows(workflows: List<Workflow>): List<UUID> {
        if (!workflows.all { activities.keys.containsAll(it.activities) }) {
            throw IllegalArgumentException("can't start a workflow with unknown activities")
        }

        return query { workflows.mapNotNull { repo.create(it) } }
    }

    override fun countWorkflows(status: WorkflowStatus) = query { repo.countWorkflows(status) }

    private fun work() {
        val sleep = query { repo.lockWorkflow(resolveActivityTimeout) }
            ?.let { runWorkflow(it) } ?: true

        if (sleep) {
            await(engineConfig.workerSleepMs)
        }
    }

    /**
     * @return true if the worker should wait before running again,
     *         false if it should try to lock another workflow immediately.
     */
    private fun runWorkflow(workflow: Workflow): Boolean {
        engineLogger("locked a workflow ${workflow.id}")

        val activityId = workflow.activities.first()

        val activity: Activity<*, *> = activities[activityId] ?: run {
            // if activity class not found, ignore the workflow (perhaps upgrade is in progress)
            query { repo.unlockWorkflow(workflow.id) }
            return true
        }

        // run activity
        engineLogger("running activity $activityId")
        val result = activity.runWrapper(workflow.data, workflow, this)
        engineLogger("$activityId success: ${result is ActivityResult.Success}")

        // if successful, remove the activity from the list, update data and unlock the workflow
        when (result) {
            is ActivityResult.Success -> {
                val completeResult =
                    query { repo.completeActivity(workflow, result.data ?: byteArrayOf(), resolveActivityTimeout) }

                engineLogger("activity complete result: $completeResult")

                return when (completeResult) {
                    is CompleteActivityResult.CompleteIntermediate -> runWorkflow(completeResult.workflow)
                    is CompleteActivityResult.CompleteTerminal -> false
                    is CompleteActivityResult.Ignored -> true
                }
            }

            // if failed but should retry, unlock the workflow for a retry
            is ActivityResult.Error -> {
                when (result.retry) {
                    true -> query { repo.unlockWorkflow(workflow.id) }
                    false -> query { repo.failActivity(workflow.id, result.exception.message) }
                }
                engineLogger("activity $activityId failed due to ${result.exception.message}")
                result.exception.printStackTrace()
                return true
            }
        }
    }

    private fun babysit() {
        engineLogger("babysitting...")
        query { repo.unlockStuckWorkflows() }
        await(engineConfig.babysitterPeriodMs)
    }

    private fun <In, Out> Activity<In, Out>.runWrapper(
        input: ByteArray,
        workflow: Workflow,
        engine: WorkflowEngineImpl
    ): ActivityResult<ByteArray> {
        val data: In = try {
            decode(input)
        } catch (e: Exception) {
            return ActivityResult.Error(e, retry = engineConfig.retryWhenSerializationFails)
        }

        val result = try {
            run(data, workflow, engine)
        } catch (e: ClassCastException) {
            return ActivityResult.Error(e, retry = false)
        }

        return when (result) {
            is ActivityResult.Success -> ActivityResult.Success(encode(result.data))
            is ActivityResult.Error -> ActivityResult.Error(result.exception, result.retry)
        }
    }
}
