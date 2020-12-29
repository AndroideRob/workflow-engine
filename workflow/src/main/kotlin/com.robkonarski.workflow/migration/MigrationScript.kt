package com.robkonarski.workflow.migration

import org.jetbrains.exposed.sql.Transaction

internal interface MigrationScript {

    val version: Int

    fun run(transaction: Transaction)

    @JvmDefault
    fun mysql(transaction: Transaction) = run(transaction)

    @JvmDefault
    fun postgres(transaction: Transaction) = run(transaction)

    @JvmDefault
    fun h2(transaction: Transaction) = run(transaction)
}
