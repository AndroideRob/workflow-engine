package com.robkonarski.workflow.data

import com.robkonarski.workflow.Activity
import com.robkonarski.workflow.EngineConfig
import com.robkonarski.workflow.WorkflowEngine
import com.robkonarski.workflow.build
import com.robkonarski.workflow.db.Schema
import com.robkonarski.workflow.engine.WorkflowEngineImpl
import com.robkonarski.workflow.engine.WorkflowRepository
import com.robkonarski.workflow.engine.WorkflowRepositoryImpl

internal fun engine(
    activities: List<Activity<*, *>> = listOf(),
    workers: Int = 1,
    repo: WorkflowRepository = WorkflowRepositoryImpl(workers, Schema.Workflows("")),
): WorkflowEngine = WorkflowEngineImpl(h2(), activities, EngineConfig(workers, 100, 200), repo = repo)

internal fun workflow(
    activities: List<Activity<*, *>> = listOf(DoNothingActivity()),
    input: Any = Input(),
    idempotencyKey: String? = null
) = build(activities, input, idempotencyKey)
