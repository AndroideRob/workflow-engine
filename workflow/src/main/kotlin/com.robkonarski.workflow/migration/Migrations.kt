package com.robkonarski.workflow.migration

import org.jetbrains.exposed.sql.Transaction

internal class _1(override val version: Int = 1) : MigrationScript {
    override fun run(transaction: Transaction): Unit = with(transaction) {
        exec("SELECT 1;")
    }
}
