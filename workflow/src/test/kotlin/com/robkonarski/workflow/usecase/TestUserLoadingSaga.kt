package com.robkonarski.workflow.usecase

import com.robkonarski.workflow.*
import com.robkonarski.workflow.data.engine
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Test
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class TestUserLoadingSaga {

    @Test
    fun `load 200 users`() {
        val counter = AtomicInteger(0)
        val engine = createEngine(counter).apply { start() }

        val workflows = (0 until 200).map { buildWorkflow() }
        assertNotNull(engine.createWorkflows(workflows))

        while (engine.countWorkflows(WorkflowStatus.Active).toInt() != 0) {
            Thread.sleep(100)
        }

        assertEquals(200, engine.countWorkflows(WorkflowStatus.Done))
        assertEquals(0, engine.countWorkflows(WorkflowStatus.Failed))
        assertEquals(200, counter.get())

        engine.stop()
    }

    private fun createEngine(counter: AtomicInteger) = engine(
        activities = listOf(
            IngestPassportActivity(),
            IngestProfileActivity(),
            IngestActionsActivity(),
            ConfirmIngestionActivity(counter)
        ),
        workers = 16
    ).apply { start() }

    private fun buildWorkflow() = buildWithClassNames(
        activities = listOf(
            IngestPassportActivity::class.java,
            IngestProfileActivity::class.java,
            IngestActionsActivity::class.java,
            ConfirmIngestionActivity::class.java
        ),
        input = generateUser()
    )

    private fun generateUser() = User(
        id = "id-${UUID.randomUUID()}",
        name = "name=${UUID.randomUUID()}",
        age = Random().nextInt(99),
        actions = List(Random().nextInt(100)) {
            Action("tid-$it", BigDecimal.valueOf(it.toLong()))
        }
    )
}

data class Action(
    val id: String = "",
    val amount: BigDecimal = BigDecimal.ZERO
)

data class User(
    val id: String = "",
    val name: String = "",
    val age: Int = -1,
    val actions: List<Action> = listOf()
)

class IngestPassportActivity : Activity<User, User> {

    override fun run(input: User, workflow: Workflow, engine: WorkflowEngine): ActivityResult<User> {
        return ActivityResult.Success(input)
    }
}

class IngestProfileActivity : Activity<User, User> {

    override fun run(input: User, workflow: Workflow, engine: WorkflowEngine): ActivityResult<User> {
        return ActivityResult.Success(input)
    }
}

class IngestActionsActivity : Activity<User, User> {

    override fun run(input: User, workflow: Workflow, engine: WorkflowEngine): ActivityResult<User> {
        return ActivityResult.Success(input)
    }
}

class ConfirmIngestionActivity(private val counter: AtomicInteger) : Activity<User, Nothing> {

    override fun run(input: User, workflow: Workflow, engine: WorkflowEngine): ActivityResult<Nothing> {
        counter.incrementAndGet()
        return ActivityResult.Success()
    }
}
