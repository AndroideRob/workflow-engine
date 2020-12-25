package com.robkonarski.workflow.engine

import com.robkonarski.workflow.Workflow
import com.robkonarski.workflow.WorkflowStatus
import com.robkonarski.workflow.core.AbstractModel
import java.time.LocalDateTime
import java.util.*

internal data class WorkflowImpl(
    override val data: ByteArray,
    override val activities: List<String>,
    override val idempotencyKey: String? = null,
    override val autoUnlockedAt: LocalDateTime? = null,
    override val status: WorkflowStatus = WorkflowStatus.Active
) : AbstractModel(), Workflow