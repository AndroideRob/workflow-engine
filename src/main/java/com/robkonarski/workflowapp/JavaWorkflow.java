package com.robkonarski.workflowapp;

import com.robkonarski.workflow.Activity;
import com.robkonarski.workflow.Workflow;
import com.robkonarski.workflow.WorkflowBuilder;
import com.robkonarski.workflowapp.examples.TestBrokerActivity;
import com.robkonarski.workflowapp.examples.TestInput;

import java.util.ArrayList;
import java.util.List;

public class JavaWorkflow {

    public static Workflow create() {
        List<Activity<?, ?>> activities = new ArrayList<>();
        activities.add(new JavaInputActivity());
        activities.add(new JavaTerminalActivity());
        return WorkflowBuilder.build(activities, new JavaInput(10, "greetings"));
    }

    public static Workflow createBroker() {
        List<Activity<?, ?>> activities = new ArrayList<>();
        activities.add(new TestBrokerActivity());
        return WorkflowBuilder.build(activities, new TestInput(10, "greetings"));
    }
}
