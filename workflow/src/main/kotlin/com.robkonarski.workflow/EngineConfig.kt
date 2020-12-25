package com.robkonarski.workflow

/**
 * Configuration of the WorkflowEngine.
 *
 * @param workers The maximum number of activities to be run in parallel.
 *        For a smaller number of heavy workloads, keep this number low.
 *        For a large number of lightweight workflows, it can be increased to increase throughput.
 *        Defaults to 2.
 * @param workerSleepMs The time in ms that a worker sleeps before attempting to execute an activity.
 *        Defaults to 1_000.
 * @param babysitterPeriodMs The time in ms between execution of a babysitter process, which unlocks stale activities.
 *        Defaults to 10_000.
 * @param retryWhenSerializationFails If true, engine will keep retrying activities if input deserialization fails.
 *        Defaults to false.
 */
data class EngineConfig @JvmOverloads constructor(
    val workers: Int = 2,
    val workerSleepMs: Long = 1_000L,
    val babysitterPeriodMs: Long = 10_000L,
    val retryWhenSerializationFails: Boolean = false
)
