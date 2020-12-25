package com.robkonarski.workflow

/**
 * @property Active Workflow is being or will be processed.
 * @property Done Workflow was successfully completed.
 * @property Failed Workflow wasn't completed.
 */
enum class WorkflowStatus {
    Active,
    Done,
    Failed
}
