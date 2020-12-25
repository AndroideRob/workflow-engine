package com.robkonarski.workflowapp;

import com.robkonarski.workflow.Activity;
import com.robkonarski.workflow.DatabaseConfig;
import com.robkonarski.workflow.EngineConfig;
import com.robkonarski.workflow.WorkflowEngine;
import com.robkonarski.workflowapp.examples.*;
import io.ktor.application.Application;

import java.util.ArrayList;
import java.util.List;

public class JavaApplication {

    public static WorkflowEngine createEngine(io.ktor.application.Application app) {
        List<Activity<?, ?>> activities = new ArrayList<>();
        activities.add(new JavaInputActivity());
        activities.add(new JavaTerminalActivity());
        activities.add(new TestInputActivity());
        activities.add(new TestStageOneActivity());
        activities.add(new TestFinalActivity());
        activities.add(new TestBrokerActivity());
        activities.add(new DemoInputActivity());
        activities.add(new DemoTerminalActivity());

        WorkflowEngine engine = WorkflowEngine.Companion.createDefault(getMysql(app), activities, new EngineConfig());
        engine.start();
        return engine;
    }

    private static DatabaseConfig getMysql(Application app) {
        return new DatabaseConfig.Mysql(
                app.getEnvironment().getConfig().property("database.mysql.url").getString(),
                app.getEnvironment().getConfig().property("database.mysql.user").getString(),
                app.getEnvironment().getConfig().property("database.mysql.password").getString(),
                ""
        );
    }
}
