package com.robkonarski.workflow.migration

import org.jetbrains.exposed.sql.Transaction

internal interface MigrationScript {
    val version: Int
    fun run(transaction: Transaction)
}
