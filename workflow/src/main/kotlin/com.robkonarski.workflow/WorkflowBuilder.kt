@file:JvmName("WorkflowBuilder")

package com.robkonarski.workflow

import com.robkonarski.workflow.engine.WorkflowImpl
import com.robkonarski.workflow.engine.encodeData

@JvmOverloads
fun <T> build(
    activities: List<Activity<*, *>>,
    input: T,
    idempotencyKey: String? = null
): Workflow {
    if (activities.isEmpty()) {
        throw IllegalArgumentException("can't start a workflow without activities")
    }

    return WorkflowImpl(
        data = activities.first().encode(input),
        activities = activities.map { it::class.java.name },
        idempotencyKey = idempotencyKey
    )
}

@JvmOverloads
fun <T> buildWithClassNames(
    activities: List<Class<out Activity<*, *>>>,
    input: T,
    idempotencyKey: String? = null
): Workflow {
    if (activities.isEmpty()) {
        throw IllegalArgumentException("can't create a workflow without activities")
    }

    return WorkflowImpl(
        data = encodeData(input),
        activities = activities.map { it.name },
        idempotencyKey = idempotencyKey
    )
}
