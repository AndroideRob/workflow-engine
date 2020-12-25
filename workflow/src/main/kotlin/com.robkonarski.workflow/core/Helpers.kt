package com.robkonarski.workflow.core

import org.jetbrains.exposed.sql.transactions.transaction

internal fun tryIgnore(predicate: () -> Unit) = try {
    predicate()
} catch (e: Exception) {
    e.printStackTrace()
}

internal fun <T> query(block: () -> T): T =
    transaction { block() }

internal val debugLog = { message: String -> println("Workflow Engine: $message") }
