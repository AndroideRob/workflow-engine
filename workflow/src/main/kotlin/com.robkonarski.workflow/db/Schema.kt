package com.robkonarski.workflow.db

import com.robkonarski.workflow.WorkflowStatus
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.statements.api.ExposedBlob

internal sealed class Schema {

    class Workflows(prefix: String) : SqlTable("${prefix}workflows") {
        val data = registerColumn<ExposedBlob>("data", LongBlobColumnType())
        val activities = text("activities")
        val idempotencyKey = varchar("idempotency_key", 255).nullable().uniqueIndex()
        val autoUnlockedAt = datetime("auto_unlocked_at").nullable().index()
        val completedAt = datetime("completed_at").nullable()
        val failureReason = varchar("failure_reason", 2000).nullable()
        val status = enumerationByName("status", 32, WorkflowStatus::class)
            .default(WorkflowStatus.Active)
            .index()
    }

    class Migrations(prefix: String) : SqlTable("${prefix}migrations") {
        val version = integer("version").default(0)
    }
}
