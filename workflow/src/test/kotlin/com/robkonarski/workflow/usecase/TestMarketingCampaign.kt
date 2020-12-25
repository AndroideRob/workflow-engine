package com.robkonarski.workflow.usecase

import com.robkonarski.workflow.*
import com.robkonarski.workflow.data.engine
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Test
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class TestMarketingCampaignUseCase {

    @Test
    fun `happy path 200k`() {
        val messagesCounter = AtomicInteger(0)
        val engine = createEngine(200_000, messagesCounter).apply { start() }

        assertNotNull(engine.createWorkflow(buildWorkflow()))

        while (engine.countWorkflows(WorkflowStatus.Active).toInt() != 0) {
            Thread.sleep(100)
        }

        assertEquals(201, engine.countWorkflows(WorkflowStatus.Done))
        assertEquals(0, engine.countWorkflows(WorkflowStatus.Failed))
        assertEquals(200, messagesCounter.get())

        engine.stop()
    }

    @Test
    fun `send failing randomly 50k`() {
        val messagesCounter = AtomicInteger(0)
        val engine = createEngine(50_000, messagesCounter, failRandomly = true).apply { start() }

        assertNotNull(engine.createWorkflow(buildWorkflow()))

        while (engine.countWorkflows(WorkflowStatus.Active).toInt() != 0) {
            Thread.sleep(100)
        }

        assertEquals(51, engine.countWorkflows(WorkflowStatus.Done))
        assertEquals(0, engine.countWorkflows(WorkflowStatus.Failed))
        assertEquals(50, messagesCounter.get())

        engine.stop()
    }

    private fun createEngine(audienceSize: Int, messagesCounter: AtomicInteger, failRandomly: Boolean = false) = engine(
        activities = listOf(
            ResolveAudienceActivity(audienceSize),
            ResolveContentActivity(),
            CreateBatchesActivity(),
            SendMessagesActivity(messagesCounter, failRandomly)
        ),
        workers = 4
    ).apply { start() }

    private fun buildWorkflow() = buildWithClassNames(
        activities = listOf(
            ResolveAudienceActivity::class.java,
            ResolveContentActivity::class.java,
            CreateBatchesActivity::class.java,
        ),
        input = CampaignData(
            id = "id",
            audienceId = "audienceId"
        ),
        idempotencyKey = "id"
    )
}


/**
 * Input models
 */
data class CampaignData(
    val id: String = "",
    val audienceId: String = ""
)

/**
 * Intermediate models
 */
data class CampaignWithAudience(
    val campaign: CampaignData = CampaignData(),
    val userIds: List<String> = emptyList(),
)

data class CampaignWithContent @JvmOverloads constructor(
    val campaign: CampaignData = CampaignData(),
    val content: String = "",
    val userIds: List<String> = listOf(""),
)

class ResolveAudienceActivity(private val size: Int) : Activity<CampaignData, CampaignWithAudience> {

    override fun run(
        input: CampaignData,
        workflow: Workflow,
        engine: WorkflowEngine
    ): ActivityResult<CampaignWithAudience> {
        val userIds = List(size) { UUID.randomUUID().toString() }
        return ActivityResult.Success(CampaignWithAudience(input, userIds))
    }
}

class ResolveContentActivity : Activity<CampaignWithAudience, CampaignWithContent> {

    override fun run(
        input: CampaignWithAudience,
        workflow: Workflow,
        engine: WorkflowEngine
    ): ActivityResult<CampaignWithContent> {
        return ActivityResult.Success(CampaignWithContent(input.campaign, "content", input.userIds))
    }
}

class CreateBatchesActivity() : Activity<CampaignWithContent, Nothing> {

    override fun run(
        input: CampaignWithContent,
        workflow: Workflow,
        engine: WorkflowEngine
    ): ActivityResult<Nothing> {
        var chunkIndex = 0
        val sendWorkflows = input.userIds.sortedDescending().chunked(1000) { chunk ->
            buildWithClassNames(
                activities = listOf(SendMessagesActivity::class.java),
                input = input.copy(userIds = chunk.toList()),
                idempotencyKey = chunkKey(input.campaign, chunkIndex++)
            )
        }

        engine.createWorkflows(sendWorkflows)
        return ActivityResult.Success()
    }

    private fun chunkKey(campaign: CampaignData, index: Int) = "campaign-${campaign.id}-$index"
}

class SendMessagesActivity(
    private val counter: AtomicInteger,
    private val failRandomly: Boolean = false
) : Activity<CampaignWithContent, Nothing> {

    override fun run(
        input: CampaignWithContent,
        workflow: Workflow,
        engine: WorkflowEngine
    ): ActivityResult<Nothing> {
        println("${System.currentTimeMillis()} - ${Thread.currentThread().id} --- ${workflow.id} sending messages to ${input.userIds.count()} users...")

        if (failRandomly && Random().nextBoolean()) {
            return ActivityResult.Error(exception = Exception("random error"), retry = true)
        }

        counter.incrementAndGet()
        return ActivityResult.Success()
    }
}
