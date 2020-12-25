package com.robkonarski.workflow

import com.robkonarski.workflow.data.*
import com.robkonarski.workflow.data.DoNothingActivity
import com.robkonarski.workflow.data.LatchActivity
import com.robkonarski.workflow.data.SplitterActivity
import junit.framework.Assert.assertNull
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class TestWorkflowIdempotence {

    @Test
    fun `one workflow created with an idempotency key`() {
        val engine = engine(listOf(DoNothingActivity()))

        val uniqueId = "unique"
        assertNotNull(engine.createWorkflow(workflow(idempotencyKey = uniqueId)))
        assertNull(engine.createWorkflow(workflow(idempotencyKey = uniqueId)))
    }

    @Test
    fun `split workflows`() {
        val latch = CountDownLatch(2)
        val latchActivity = LatchActivity(latch)
        val splitterActivity = SplitterActivity {
            listOf(
                workflow(listOf(latchActivity)),
                workflow(listOf(latchActivity))
            )
        }

        val engine = engine(listOf(latchActivity, splitterActivity)).apply { start() }
        engine.createWorkflow(workflow(listOf(splitterActivity)))

        if (!latch.await(3, TimeUnit.SECONDS)) {
            Assert.fail("workflows were not split successfully")
        }
        Thread.sleep(100)

        assertEquals(0, engine.countWorkflows(WorkflowStatus.Active))

        engine.stop()
    }
}
