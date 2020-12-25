package com.robkonarski.workflow.migration

import com.robkonarski.workflow.db.Schema
import com.robkonarski.workflow.core.CrudRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

internal class MigrationsRepository(schema: Schema.Migrations) :
    CrudRepository<Schema.Migrations, Migration>(table = schema) {

    override fun ResultRow.toObject() = Migration(
        version = this@toObject[table.version]
    ).applyDefaults(this)

    override fun insert(statement: InsertStatement<*>, view: Migration) {
        statement[table.version] = view.version
    }

    override fun update(statement: UpdateStatement, view: Migration) {
        statement[table.version] = view.version
    }

    fun find() = findAll().firstOrNull()
        ?: create(Migration(0))?.let { findById(it) }
        ?: throw IllegalStateException("Could not create a migration entry")

    private fun Migration.applyDefaults(resultRow: ResultRow) = apply {
        id = resultRow[table.id].value
        dateCreated = resultRow[table.dateCreated]
        dateUpdated = resultRow[table.dateUpdated]
    }
}