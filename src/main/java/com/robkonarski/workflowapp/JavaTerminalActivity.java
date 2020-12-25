package com.robkonarski.workflowapp;

import com.robkonarski.workflow.Activity;
import com.robkonarski.workflow.ActivityResult;
import com.robkonarski.workflow.Workflow;
import com.robkonarski.workflow.WorkflowEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaTerminalActivity implements Activity<JavaOutput, Void> {

    @NotNull
    @Override
    public ActivityResult<Void> run(@NotNull JavaOutput input, @NotNull Workflow workflow, @NotNull WorkflowEngine engine) {
        return new ActivityResult.Success<>();
    }
}
