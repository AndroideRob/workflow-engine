package com.robkonarski.workflow.engine

import com.robkonarski.workflow.Workflow
import com.robkonarski.workflow.WorkflowStatus
import com.robkonarski.workflow.core.CrudRepository
import com.robkonarski.workflow.db.Schema
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import java.time.LocalDateTime
import java.util.*

internal class WorkflowRepositoryImpl(private val lockingParallelism: Int, schema: Schema.Workflows) :
    CrudRepository<Schema.Workflows, Workflow>(table = schema),
    WorkflowRepository {

    override fun ResultRow.toObject() = WorkflowImpl(
        data = this@toObject[table.data].bytes,
        activities = this@toObject[table.activities].list(),
        idempotencyKey = this@toObject[table.idempotencyKey],
        autoUnlockedAt = this@toObject[table.autoUnlockedAt],
        status = this@toObject[table.status]
    ).applyDefaults(this)

    override fun insert(statement: InsertStatement<*>, view: Workflow) {
        statement[table.data] = ExposedBlob(view.data)
        statement[table.activities] = view.activities.unlist()
        statement[table.idempotencyKey] = view.idempotencyKey
        statement[table.autoUnlockedAt] = view.autoUnlockedAt
        statement[table.status] = view.status
    }

    override fun update(statement: UpdateStatement, view: Workflow) {
        statement[table.data] = ExposedBlob(view.data)
        statement[table.activities] = view.activities.unlist()
        statement[table.idempotencyKey] = view.idempotencyKey
        statement[table.autoUnlockedAt] = view.autoUnlockedAt
        statement[table.status] = view.status
    }

    override fun lockWorkflow(autoUnlockAfterSeconds: (activity: String) -> Int): Workflow? {
        // TODO optimize for large objects
        val workflow = table.select { (table.autoUnlockedAt.isNull()) and (table.status eq WorkflowStatus.Active) }
            .orderBy(table.dateCreated, SortOrder.ASC)
            .limit(lockingParallelism)
            .mapNotNull { it.toObject() }
            .shuffled()
            .firstOrNull()
            ?: return null

        val updatedRows =
            table.update(where = {
                table.id eq workflow.id and table.autoUnlockedAt.isNull() and (table.status eq WorkflowStatus.Active)
            }) {
                it[table.autoUnlockedAt] =
                    calculateAutoUnlockedAt(workflow.activities.firstOrNull(), autoUnlockAfterSeconds)
                it[table.dateUpdated] = LocalDateTime.now()
            }

        return when (updatedRows) {
            1 -> workflow
            else -> null
        }
    }

    override fun unlockStuckWorkflows() = table.update(where = { table.autoUnlockedAt.less(LocalDateTime.now()) }) {
        it[table.autoUnlockedAt] = null
        it[table.dateUpdated] = LocalDateTime.now()
    } == 1

    override fun unlockWorkflow(id: UUID) = table.update(where = { table.id eq id }) {
        it[table.autoUnlockedAt] = null
        it[table.dateUpdated] = LocalDateTime.now()
    } == 1

    override fun completeActivity(
        workflow: Workflow,
        data: ByteArray,
        autoUnlockAfterSeconds: (activity: String) -> Int
    ): CompleteActivityResult {
        if (workflow.activities.isEmpty()) return CompleteActivityResult.Ignored

        val newActivities = workflow.activities.toMutableList().apply { removeAt(0) }

        val updated =
            table.update(where = { table.id eq workflow.id and (table.activities eq workflow.activities.unlist()) }) {
                it[table.activities] = newActivities.unlist()
                it[table.dateUpdated] = LocalDateTime.now()

                if (newActivities.isEmpty()) {
                    it[table.status] = WorkflowStatus.Done
                    it[table.data] = ExposedBlob(byteArrayOf())
                    it[table.completedAt] = LocalDateTime.now()
                    it[table.autoUnlockedAt] = null
                } else {
                    it[table.data] = ExposedBlob(data)
                    it[table.autoUnlockedAt] = calculateAutoUnlockedAt(newActivities.first(), autoUnlockAfterSeconds)
                }
            } == 1

        return when {
            !updated -> CompleteActivityResult.Ignored
            newActivities.isEmpty() -> CompleteActivityResult.CompleteTerminal
            else -> CompleteActivityResult.CompleteIntermediate(findById(workflow.id) ?: workflow)
        }
    }

    override fun failActivity(id: UUID, reason: String?) = table.update(where = { table.id eq id }) {
        it[table.status] = WorkflowStatus.Failed
        it[table.failureReason] = reason
        it[table.completedAt] = LocalDateTime.now()
        it[table.dateUpdated] = LocalDateTime.now()
        it[table.autoUnlockedAt] = null
    } == 1

    override fun countWorkflows(status: WorkflowStatus) = table.select { table.status eq status }.count()

    private fun WorkflowImpl.applyDefaults(resultRow: ResultRow) = apply {
        id = resultRow[table.id].value
        dateCreated = resultRow[table.dateCreated]
        dateUpdated = resultRow[table.dateUpdated]
    }

    private fun calculateAutoUnlockedAt(
        activity: String?,
        autoUnlockAfterSeconds: (activity: String) -> Int
    ): LocalDateTime {
        val autoUnlockAfter = activity?.let { autoUnlockAfterSeconds(it) } ?: 60
        return LocalDateTime.now().plusSeconds(autoUnlockAfter.toLong())
    }

    companion object {
        private const val ActivitySeparator = ";"
        private fun List<String>.unlist() = joinToString(ActivitySeparator)
        private fun String.list() = split(ActivitySeparator)
    }
}
