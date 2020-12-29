## Workflow Engine

A library adding workflow orchestration capability to your JVM applications. 
It is completely decentralized, just connect your database and give it a go.


### Main benefits
- Decentralized - just a library to include in your service
- Fault-tolerant - at-least-once execution guarantees
- Efficient - intermediate state is persisted, no need to restart the whole workflow in case of failures
- Flexible - can be configured to efficiently process a large number of small workflows, or a small number of heavy workflows
- Scalable - horizontal scalability by sharding tables (coming soon)


### How to run an example

#### H2
- Run the `main()` method inside `Application.kt` to start the service
- call `localhost/demo` and have fun looking at the logs

#### Mysql/Postgres
- Modify `Application.createEngine()` method: comment out `h2`, uncomment your preferred database config
- Run the `main()` method inside `Application.kt` to start the service
- call `localhost/demo` and have fun looking at the database


### How to import an artifact
Artifacts are publicly available on [Bintray Maven Repo](https://bintray.com/androiderob/workflow-engine/engine#).

#### Gradle
```
# build.gradle.kts

repositories {
    maven("https://dl.bintray.com/androiderob/workflow-engine")
}

dependencies {
    implementation("com.robkonarski.workflow:engine:0.0.5")
}
```


### Activity requirements/recommendations
- Activities must be immutable - their inputs/outputs must not change
- Activities should be idempotent - since we can only provide at-least-once delivery guarantees
- If the Activity's input or output needs to change - copy the activity
- If the Activity is used in a workflow - don't delete it