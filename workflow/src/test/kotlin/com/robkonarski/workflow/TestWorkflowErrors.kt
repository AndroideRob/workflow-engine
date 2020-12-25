package com.robkonarski.workflow

import com.robkonarski.workflow.data.FailingActivity
import com.robkonarski.workflow.data.FirstTimeFailingActivity
import com.robkonarski.workflow.data.engine
import com.robkonarski.workflow.data.workflow
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class TestWorkflowErrors {

    @Test
    fun `test activity failing retryably once`() {
        val latch = CountDownLatch(1)
        val activity = FirstTimeFailingActivity(latch)
        val engine = engine(listOf(activity)).apply { start() }

        engine.createWorkflow(workflow(listOf(activity)))

        if (!latch.await(3, TimeUnit.SECONDS)) {
            Assert.fail("activity wasn't successfully restarted and completed")
        }

        engine.stop()
    }

    @Test
    fun `test activity failing unretryably`() {
        val activity = FailingActivity()
        val engine = engine(listOf(activity)).apply { start() }

        engine.createWorkflow(workflow(listOf(activity)))

        Thread.sleep(1500)

        assertEquals(0, engine.countWorkflows(WorkflowStatus.Active))
    }
}
