package com.robkonarski.workflow

import com.robkonarski.workflow.engine.decodeData
import com.robkonarski.workflow.engine.encodeData

/**
 * The smallest unit of execution, essentially a java.util.Function on steroids.
 * One of more activities form a Workflow, when Out type of the previous activity matches the In type of the next one.
 *
 * It's possible to have two kinds of activities: intermediate (input, output) and terminal (input).
 * In the case of a terminal activity, specify Nothing (Kotlin) or Void (Java) as the Out type.
 *
 * @param <In> Input type. Must have a no-arg constructor.
 * @param <Out> Output type. Must have a no-arg constructor.
</Out></In> */
interface Activity<In, Out> {

    /**
     * Override with your business logic.
     * The operation must be idempotent if exactly-one semantics are required.
     *
     * @param input Activity input, which is either the output of the previous activity, or the workflow input.
     * @param workflow The workflow in the context of which the activity is executed.
     * @param engine The engine executing the activity.
     * @return ActivityResult.Success when an activity completed successfully, ActivityResult.Error otherwise.
     */
    fun run(input: In, workflow: Workflow, engine: WorkflowEngine): ActivityResult<Out>

    /**
     * @return The time in seconds after which the activity is considered timed out, and will be restarted.
     * Defaults to 60 seconds.
     */
    @JvmDefault
    fun timeoutSeconds(): Int = 60

    /**
     * @return Unique identifier of the activity, used by the WorkflowEngine to decide which activity to run.
     */
    @JvmDefault
    fun uniqueId(): String? = javaClass.name

    /**
     * @param data JVM object.
     * @return Serialized binary object, written to the database.
     * By default, uses Kryo serialization.
     */
    @JvmDefault
    fun encode(data: Any?): ByteArray {
        return encodeData(data)
    }

    /**
     * @param input Binary input coming from the database.
     * @return Deserialized JVM object, of the activity input type.
     * By default, uses Kryo deserialization.
     */
    @JvmDefault
    fun decode(input: ByteArray): In {
        return decodeData(input)
    }
}