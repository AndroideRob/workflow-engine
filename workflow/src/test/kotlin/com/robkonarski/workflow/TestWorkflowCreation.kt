package com.robkonarski.workflow

import com.robkonarski.workflow.data.Input
import com.robkonarski.workflow.data.LatchActivity
import com.robkonarski.workflow.data.engine
import com.robkonarski.workflow.data.workflow
import org.junit.Test
import java.util.concurrent.CountDownLatch

class TestWorkflowCreation {

    @Test(expected = IllegalArgumentException::class)
    fun `fail creating a workflow with empty activities`() {
        engine().createWorkflow(build(listOf(), Unit))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fail creating a workflow with class names with empty activities`() {
        engine().createWorkflow(buildWithClassNames(listOf(), Unit))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fail creating multiple workflows with empty activities`() {
        engine().createWorkflows(listOf(workflow(), workflow()))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fail creating a workflow with an unregistered activity`() {
        val engine = engine(listOf(LatchActivity(CountDownLatch(0))))
        engine.createWorkflow(workflow())
    }

    @Test
    fun `create with activity class names`() {
        val engine = engine(listOf(LatchActivity(CountDownLatch(0))))
        engine.createWorkflow(buildWithClassNames(listOf(LatchActivity::class.java), Input()))
    }
}
