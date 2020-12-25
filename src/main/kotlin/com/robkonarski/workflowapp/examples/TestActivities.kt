package com.robkonarski.workflowapp.examples

import com.robkonarski.workflow.Activity
import com.robkonarski.workflow.ActivityResult
import com.robkonarski.workflow.Workflow
import com.robkonarski.workflow.WorkflowEngine
import java.util.concurrent.atomic.AtomicInteger

data class TestInput @JvmOverloads constructor(
    val audienceSize: Int = 10,
    val appName: String = "testname"
)

data class TestStageOne @JvmOverloads constructor(
    val userIds: List<String> = listOf(),
    val appName: String = ""
)

data class TestFinalStage @JvmOverloads constructor(
    val messages: List<String> = listOf()
)

val messageCounter = AtomicInteger()

class TestInputActivity : Activity<TestInput, TestStageOne> {

    override fun timeoutSeconds() = 10

    override fun run(input: TestInput, workflow: Workflow, engine: WorkflowEngine): ActivityResult<TestStageOne> {
        return ActivityResult.Success(
            TestStageOne(
                userIds = (0 until input.audienceSize).map { it.toString() },
                appName = input.appName
            )
        )
    }
}

class TestStageOneActivity : Activity<TestStageOne, TestFinalStage> {

    override fun timeoutSeconds() = 10

    override fun run(input: TestStageOne, workflow: Workflow, engine: WorkflowEngine): ActivityResult<TestFinalStage> {
        return ActivityResult.Success(TestFinalStage(input.userIds.map { "$it-message" }))
    }
}

class TestFinalActivity : Activity<TestFinalStage, Nothing> {
    override fun run(input: TestFinalStage, workflow: Workflow, engine: WorkflowEngine): ActivityResult<Nothing> {
        input.messages.chunked(1000).forEachIndexed { _, messages ->
            (messages.indices).forEach { messageCounter.incrementAndGet() }
        }

        return ActivityResult.Success()
    }
}

class TestBrokerActivity : Activity<TestInput, Void> {

    override fun timeoutSeconds() = 10

    override fun run(input: TestInput, workflow: Workflow, engine: WorkflowEngine): ActivityResult<Void> {
        return ActivityResult.Success()
    }
}
