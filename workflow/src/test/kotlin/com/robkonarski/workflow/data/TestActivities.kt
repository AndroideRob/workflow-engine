package com.robkonarski.workflow.data

import com.robkonarski.workflow.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

data class Input(
    val number: Int = 100,
    val test: String = "one hundred"
)

data class Output(
    val messages: List<String> = listOf()
)

class LatchActivity(
    private val latch: CountDownLatch,
    private val extraWork: () -> Unit = {}
) : Activity<Input, Nothing> {

    override fun timeoutSeconds() = 1

    override fun run(input: Input, workflow: Workflow, engine: WorkflowEngine): ActivityResult<Nothing> {
        extraWork()
        latch.countDown()
        return ActivityResult.Success()
    }
}

class CounterActivity(private val count: AtomicInteger, private val oneTimeExtraWork: () -> Unit) :
    Activity<Input, Nothing> {

    var extraWorkDone = false

    override fun timeoutSeconds() = 1

    override fun run(input: Input, workflow: Workflow, engine: WorkflowEngine): ActivityResult<Nothing> {
        count.incrementAndGet()
        if (!extraWorkDone) {
            oneTimeExtraWork()
            extraWorkDone = true
        }
        return ActivityResult.Success()
    }
}

class InputActivity : Activity<Input, Output> {
    override fun run(input: Input, workflow: Workflow, engine: WorkflowEngine): ActivityResult<Output> {
        return ActivityResult.Success(Output((0 until input.number).map { input.test }))
    }
}

class DoNothingActivity : Activity<Input, Nothing> {
    override fun run(input: Input, workflow: Workflow, engine: WorkflowEngine): ActivityResult<Nothing> {
        return ActivityResult.Success()
    }
}

class FailingActivity : Activity<Input, Nothing> {
    override fun run(input: Input, workflow: Workflow, engine: WorkflowEngine): ActivityResult<Nothing> {
        return ActivityResult.Error(Exception(), retry = false)
    }
}

class FirstTimeFailingActivity(private val latch: CountDownLatch) : Activity<Input, Nothing> {

    var failed = false

    override fun run(input: Input, workflow: Workflow, engine: WorkflowEngine): ActivityResult<Nothing> {
        if (!failed) {
            failed = true
            return ActivityResult.Error(exception = Exception(), retry = true)
        }

        latch.countDown()
        return ActivityResult.Success()
    }
}

class SplitterActivity(val split: () -> List<Workflow>) : Activity<Input, Nothing> {
    override fun run(input: Input, workflow: Workflow, engine: WorkflowEngine): ActivityResult<Nothing> {
        engine.createWorkflows(split())
        return ActivityResult.Success()
    }
}

class IOLatchActivity(
    private val latch: CountDownLatch,
    private val extraWork: () -> Unit = {}
) : Activity<Input, Input> {

    override fun run(input: Input, workflow: Workflow, engine: WorkflowEngine): ActivityResult<Input> {
        extraWork()
        latch.countDown()
        return ActivityResult.Success(input)
    }
}
