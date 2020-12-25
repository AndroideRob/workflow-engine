package com.robkonarski.workflow.engine

import com.robkonarski.workflow.Workflow

internal sealed class CompleteActivityResult {
    object Ignored : CompleteActivityResult()
    class CompleteIntermediate(val workflow: Workflow) : CompleteActivityResult()
    object CompleteTerminal : CompleteActivityResult()
}
