package com.robkonarski.workflowapp;

import com.robkonarski.workflow.Activity;
import com.robkonarski.workflow.ActivityResult;
import com.robkonarski.workflow.Workflow;
import com.robkonarski.workflow.WorkflowEngine;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class JavaInputActivity implements Activity<JavaInput, JavaOutput> {

    @NotNull
    @Override
    public ActivityResult<JavaOutput> run(@NotNull JavaInput input, @NotNull Workflow workflow, @NotNull WorkflowEngine engine) {
        List<String> titles = new ArrayList<>();
        for (int i = 0; i < input.getAge(); i++) {
            titles.add(i + ": " + input.getCelebration());
        }
        return new ActivityResult.Success<>(new JavaOutput(titles));
    }
}
