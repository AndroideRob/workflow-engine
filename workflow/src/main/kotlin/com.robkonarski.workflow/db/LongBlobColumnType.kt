package com.robkonarski.workflow.db

import org.jetbrains.exposed.sql.BlobColumnType
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.vendors.MysqlDialect
import org.jetbrains.exposed.sql.vendors.currentDialect

internal class LongBlobColumnType : IColumnType by BlobColumnType() {
    override fun sqlType(): String = when (currentDialect.name) {
        MysqlDialect.dialectName -> "LONGBLOB"
        else -> currentDialect.dataTypeProvider.blobType()
    }
}
