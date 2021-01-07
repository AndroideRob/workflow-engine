package com.robkonarski.workflow.db

import com.robkonarski.workflow.DatabaseConfig
import com.robkonarski.workflow.migration.MigrationService
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

@JvmOverloads
internal fun connectDatabase(
    config: DatabaseConfig,
    desiredParallelism: Int,
    schemas: List<SqlTable>,
    migrationService: MigrationService,
    logger: SqlLogger? = StdOutSqlLogger,
    additionalHikariConfig: HikariConfig.() -> Unit = {}
) {

    fun resolveMaxConnections() = if (desiredParallelism < config.maxConnections)
        desiredParallelism
    else config.maxConnections

    fun DatabaseConfig.resolveDriver() = when (this) {
        is DatabaseConfig.Postgres -> "org.postgresql.Driver"
        is DatabaseConfig.Mysql -> "com.mysql.jdbc.Driver"
        is DatabaseConfig.Oracle -> "oracle.jdbc.driver.OracleDriver"
        is DatabaseConfig.H2 -> "org.h2.Driver"
    }

    fun hikari(config: DatabaseConfig) = HikariDataSource(
        HikariConfig().apply {
            driverClassName = config.resolveDriver()
            jdbcUrl = config.url
            username = config.user
            password = config.password
            maximumPoolSize = resolveMaxConnections()
            isAutoCommit = false
            transactionIsolation = config.transactionIsolation
            additionalHikariConfig()
            validate()
        })

    while (true) {
        try {
            Database.connect(hikari(config))
            break
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // for tests
    if ((config as? DatabaseConfig.H2)?.clearDatabase == true) {
        transaction {
            SchemaUtils.drop(*schemas.toTypedArray())
        }
    }

    transaction {
        logger?.let { addLogger(it) }
        SchemaUtils.create(*schemas.toTypedArray())
        migrationService.migrate()
    }
}
