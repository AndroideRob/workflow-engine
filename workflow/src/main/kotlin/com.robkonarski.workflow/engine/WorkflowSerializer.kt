@file:JvmName("WorkflowSerializer")

package com.robkonarski.workflow.engine

import com.esotericsoftware.kryo.kryo5.Kryo
import com.esotericsoftware.kryo.kryo5.io.Input
import com.esotericsoftware.kryo.kryo5.io.Output
import java.io.ByteArrayOutputStream

internal fun encodeData(data: Any?): ByteArray {
    if (data == null) return byteArrayOf()

    val kryo = Kryo().apply { isRegistrationRequired = false }
    val stream = ByteArrayOutputStream()
    Output(stream).use {
        kryo.writeClassAndObject(it, data)
        it.flush()
    }
    return stream.use { it.toByteArray() }
}

fun <T> decodeData(input: ByteArray): T {
    val kryo = Kryo().apply { isRegistrationRequired = false }
    val stream = Input(input)
    return stream.use {
        kryo.readClassAndObject(stream)
    } as T
}
