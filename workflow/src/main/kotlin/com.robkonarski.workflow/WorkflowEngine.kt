package com.robkonarski.workflow

import com.robkonarski.workflow.core.debugLog
import com.robkonarski.workflow.engine.WorkflowEngineImpl
import com.zaxxer.hikari.HikariConfig
import org.jetbrains.exposed.sql.SqlLogger
import java.util.*

interface WorkflowEngine {

    /**
     * Start workers executing running activities.
     */
    fun start()

    /**
     * Stop workers executing activities. Running activities are not interrupted.
     */
    fun stop()

    /**
     * @param workflow Workflow to be created.
     * @return id of a created workflow, or null if it wasn't created.
     */
    fun createWorkflow(workflow: Workflow): UUID?

    /**
     * @param workflows List of workflows to be created atomically.
     * @return a list of ids of created workflows.
     */
    fun createWorkflows(workflows: List<Workflow>): List<UUID>

    /**
     * @param status Workflow status filter.
     * @return number of workflows.
     */
    fun countWorkflows(status: WorkflowStatus): Long

    companion object {

        /**
         * Create a workflow engine.
         *
         * @param databaseConfig Database configuration.
         * @param registeredActivities A list of all activities present in the system.
         * @param engineConfig Engine configuration.
         * @param startWorker A function executed to start a worker. Defaults to a new [Thread].
         * @param await A function the worker uses to sleep when waiting for the next activity. Defaults to a new [Thread].
         * @param engineLogger Custom logger for the Workflow Engine Implementation.
         * @param databaseLogger Custom logger for the Database Implementation.
         * @param additionalHikariConfig Custom Hikari parameters.
         */
        @JvmOverloads
        fun createDefault(
            databaseConfig: DatabaseConfig,
            registeredActivities: List<Activity<*, *>>,
            engineConfig: EngineConfig = EngineConfig(),
            startWorker: (() -> Unit) -> Unit = { Thread { it() }.start() },
            await: ((delay: Long) -> Unit) = { Thread.sleep(it) },
            engineLogger: (message: String) -> Unit = debugLog,
            databaseLogger: SqlLogger? = null,
            additionalHikariConfig: HikariConfig.() -> Unit = {},
        ): WorkflowEngine = WorkflowEngineImpl(
            databaseConfig,
            registeredActivities,
            engineConfig,
            startWorker,
            await,
            engineLogger,
            databaseLogger,
            additionalHikariConfig,
        )
    }
}
