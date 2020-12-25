package com.robkonarski.workflow.migration

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

internal class MigrationService(private val repo: MigrationsRepository, private val logger: (String) -> Unit) {

    private val scripts = arrayOf<MigrationScript>(
        _1()
    ).sortedBy { it.version }

    fun migrate() = transaction {
        if (scripts.isEmpty()) return@transaction

        val migration = repo.find()

        if (migration.version >= scripts.last().version) {
            logger("existing database version ${migration.version}, no need to migrate")
            return@transaction
        }

        val requiredScripts = scripts
            .filter { it.version > migration.version }
            .sortedBy { it.version }

        runScripts(requiredScripts, migration)
    }

    private fun Transaction.runScripts(scripts: List<MigrationScript>, migration: Migration) {
        if (scripts.isEmpty()) return

        logger("starting database migration from version: ${migration.version} to version: ${scripts.last().version}")

        scripts.forEach { script ->
            logger("applying migration script version: ${script.version}")

            script.run(this)
            repo.update(migration.id, migration.copy(version = script.version))

            logger("migration script applied. Current database version: ${script.version}")
        }
    }
}
