package com.robkonarski.workflow

import java.time.LocalDateTime
import java.util.*

/**
 * A high-level unit of execution, composed of one or more Activities.
 * WorkflowEngine takes care of executing steps within workflows and persisting the intermediate state to the database.
 *
 * @property id Unique internal identifier of a workflow
 * @property dateCreated UTC workflow creation timestamp
 * @property dateUpdated UTC workflow last update timestamp
 * @property data Binary workflow data. Could be large, depending on the use case.
 * @property activities An ordered list of activity unique ids composing a workflow.
 * @property idempotencyKey A unique external identifier of a workflow. Used when idempotency is required.
 * @property autoUnlockedAt Indicates when the workflow should be considered timed out and automatically unlocked.
 * @property status Workflow status.
 */
interface Workflow {
    val id: UUID
    val dateCreated: LocalDateTime
    val dateUpdated: LocalDateTime
    val data: ByteArray
    val activities: List<String>
    val idempotencyKey: String?
    val autoUnlockedAt: LocalDateTime?
    val status: WorkflowStatus
}
