package com.robkonarski.workflow.core

import com.robkonarski.workflow.db.SqlTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import java.time.LocalDateTime
import java.util.*

/**
 * Abstract repository for CRUD operations on relational data models.
 * Takes care of date updates and SQL exception handling, fully customizable.
 * Limitations: must be used with a [SqlTable].
 */
internal abstract class CrudRepository<T : SqlTable, M>(val table: T) {

    /**
     * Convert [ResultRow] to a POJO model
     */
    abstract fun ResultRow.toObject(): M

    /**
     * Set statement fields during an insert transaction
     */
    abstract fun insert(statement: InsertStatement<*>, view: M)

    /**
     * Set statement fields during an update transaction
     */
    abstract fun update(statement: UpdateStatement, view: M)

    open fun findAll() = table.selectAll().map { it.toObject() }

    open fun findById(id: UUID): M? {
        return table.select { table.id eq EntityID(id, table) }.map { it.toObject() }.firstOrNull()
    }

    open fun create(view: M): UUID? {
        val date = LocalDateTime.now()

        return try {
            table.insertAndGetId {
                it[dateCreated] = date
                it[dateUpdated] = date
                insert(it, view)
            }.value
        } catch (e: ExposedSQLException) {
            null
        }
    }

    open fun update(id: UUID, view: M): Boolean {
        return try {
            table.update({ table.id eq id }) {
                it[dateUpdated] = LocalDateTime.now()
                update(it, view)
            } == 1
        } catch (e: ExposedSQLException) {
            e.printStackTrace()
            false
        }
    }
}
