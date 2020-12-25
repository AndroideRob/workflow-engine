package com.robkonarski.workflowapp.examples

import com.robkonarski.workflow.*

data class DemoInput(
    val numberOfUsers: Int = 0,
    val message: String = ""
)

data class EnhancedDemoPayload(
    val messages: List<String> = listOf()
)

class DemoInputActivity : Activity<DemoInput, EnhancedDemoPayload> {
    override fun run(input: DemoInput, workflow: Workflow, engine: WorkflowEngine) =
        ActivityResult.Success(EnhancedDemoPayload((0 until input.numberOfUsers).map { "$it: ${input.message}" }))
}

class DemoTerminalActivity : Activity<EnhancedDemoPayload, Nothing> {
    override fun run(input: EnhancedDemoPayload, workflow: Workflow, engine: WorkflowEngine): ActivityResult<Nothing> {
        input.messages.forEach {
            // send message
            println("sending message: $it")
            Thread.sleep(100)
        }

        return ActivityResult.Success()
    }
}

fun demoWorkflow(input: DemoInput, count: Int = 1) = (0 until count).map {
    buildWithClassNames(
        activities = listOf(
            DemoInputActivity::class.java,
            DemoTerminalActivity::class.java
        ),
        input = input
    )
}