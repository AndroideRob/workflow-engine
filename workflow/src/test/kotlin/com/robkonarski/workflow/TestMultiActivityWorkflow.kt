package com.robkonarski.workflow

import com.robkonarski.workflow.data.*
import com.robkonarski.workflow.data.engine
import com.robkonarski.workflow.data.workflow
import junit.framework.Assert.assertEquals
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class TestMultiActivityWorkflow {

    @Test
    fun `correct multi activity workflow success`() {
        val latch = CountDownLatch(3)
        val engine = engine(listOf(IOLatchActivity(latch) { Thread.sleep(10) })).apply { start() }

        val workflowActivities = listOf(
            IOLatchActivity(latch),
            IOLatchActivity(latch),
            IOLatchActivity(latch)
        )

        engine.createWorkflow(workflow(workflowActivities))

        if (!latch.await(1, TimeUnit.SECONDS)) {
            Assert.fail("invoked ${latch.count} instead of 3 times")
        }

        engine.stop()
    }

    @Test
    fun `incorrect multi activity workflow failure`() {
        val engine = engine(listOf(InputActivity(), DoNothingActivity())).apply { start() }

        val workflowActivities = listOf(
            InputActivity(),
            DoNothingActivity()
        )

        engine.createWorkflow(workflow(workflowActivities))
        Thread.sleep(500)

        assertEquals(0, engine.countWorkflows(WorkflowStatus.Done))
        assertEquals(1, engine.countWorkflows(WorkflowStatus.Failed))

        engine.stop()
    }
}