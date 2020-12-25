package com.robkonarski.workflow.db

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.datetime

internal abstract class SqlTable(name: String) : UUIDTable(name) {
    val dateCreated = datetime("date_created")
    val dateUpdated = datetime("date_updated")
}
