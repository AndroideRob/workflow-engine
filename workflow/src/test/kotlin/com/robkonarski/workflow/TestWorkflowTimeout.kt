package com.robkonarski.workflow

import com.robkonarski.workflow.data.*
import com.robkonarski.workflow.db.Schema
import com.robkonarski.workflow.engine.CompleteActivityResult
import com.robkonarski.workflow.engine.WorkflowRepositoryImpl
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class TestWorkflowTimeout {

    @Test
    fun `test activity timeout retry`() {
        val counter = AtomicInteger(0)
        val activity = CounterActivity(counter) { Thread.sleep(1500) }
        val engine = engine(listOf(activity), workers = 2).apply { start() }

        engine.createWorkflow(workflow(listOf(activity)))

        Thread.sleep(2000)
        assertEquals(2, counter.get())

        engine.stop()
    }

    @Test
    fun `test activity timeout, retry and only complete once`() {
        val counter = AtomicInteger(0)
        val activity = CounterActivity(counter) { Thread.sleep(1500) }
        val repo = spyk(WorkflowRepositoryImpl(1, Schema.Workflows("")))
        val engine = engine(listOf(activity), repo = repo, workers = 2).apply { start() }

        engine.createWorkflow(workflow(listOf(activity)))

        // TODO figure out how to check return values
        verify(timeout = 2000) {
            repo.completeActivity(any(), any(), any())
            repo.completeActivity(any(), any(), any())
        }

        engine.stop()
    }
}
