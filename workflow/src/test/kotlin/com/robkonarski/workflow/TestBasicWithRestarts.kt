package com.robkonarski.workflow

import com.robkonarski.workflow.data.LatchActivity
import com.robkonarski.workflow.data.engine
import com.robkonarski.workflow.data.workflow
import org.junit.Assert.fail
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class TestBasicWithRestarts {

    @Test
    fun `single thread latch, one restart`() {
        val latch = CountDownLatch(20)

        val engine = engine(listOf(LatchActivity(latch)))
        engine.createWorkflows((0 until 20).map { workflow(listOf(LatchActivity(latch))) })

        engine.start()
        Thread.sleep(500)
        engine.stop()
        Thread.sleep(500)
        engine.start()

        if (!latch.await(10, TimeUnit.SECONDS)) {
            fail("invoked ${latch.count} instead of 100 times")
        }
        engine.stop()
    }

    @Test
    fun `multi thread latch, one restart`() {
        val latch = CountDownLatch(20)

        val engine = engine(listOf(LatchActivity(latch)), 8)
        engine.createWorkflows((0 until 20).map { workflow(listOf(LatchActivity(latch))) })

        engine.start()
        Thread.sleep(500)
        engine.stop()
        Thread.sleep(500)
        engine.start()

        if (!latch.await(10, TimeUnit.SECONDS)) {
            fail("invoked ${latch.count} instead of 100 times")
        }
        engine.stop()
    }
}
