package com.robkonarski.workflow

import com.robkonarski.workflow.data.Input
import com.robkonarski.workflow.data.LatchActivity
import com.robkonarski.workflow.data.engine
import org.junit.Assert.fail
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class TestBasicNoRestarts {

    @Test
    fun `single thread latch, no restarts`() {
        val latch = CountDownLatch(100)

        val engine = engine(listOf(LatchActivity(latch)))
        engine.createWorkflows((0 until 100).map { build(listOf(LatchActivity(latch)), Input()) })

        engine.start()
        if (!latch.await(20, TimeUnit.SECONDS)) {
            fail("invoked ${latch.count} instead of 100 times")
        }
        engine.stop()
    }

    @Test
    fun `multi thread latch, no restarts`() {
        val latch = CountDownLatch(100)

        val engine = engine(listOf(LatchActivity(latch)), 8)
        engine.createWorkflows((0 until 100).map { build(listOf(LatchActivity(latch)), Input()) })

        engine.start()
        if (!latch.await(10, TimeUnit.SECONDS)) {
            fail("invoked ${latch.count} instead of 100 times")
        }
        engine.stop()
    }
}
