package com.robkonarski.workflow.engine

import com.robkonarski.workflow.Workflow
import com.robkonarski.workflow.WorkflowStatus
import java.util.*

internal interface WorkflowRepository {

    fun findById(id: UUID): Workflow?

    fun create(view: Workflow): UUID?

    fun lockWorkflow(autoUnlockAfterSeconds: (activity: String) -> Int): Workflow?

    fun unlockStuckWorkflows(): Boolean

    fun unlockWorkflow(id: UUID): Boolean

    fun completeActivity(
        workflow: Workflow,
        data: ByteArray,
        autoUnlockAfterSeconds: (activity: String) -> Int
    ): CompleteActivityResult

    fun failActivity(id: UUID, reason: String?): Boolean

    fun countWorkflows(status: WorkflowStatus): Long
}
