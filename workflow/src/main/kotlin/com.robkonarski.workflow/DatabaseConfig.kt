package com.robkonarski.workflow

sealed class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
    val tablePrefix: String,
    val maxConnections: Int,
    val transactionIsolation: String
) {

    /**
     * @param maxConnections Defaults to 10.
     * @param transactionIsolation Defaults to "TRANSACTION_REPEATABLE_READ"
     */
    class Postgres @JvmOverloads constructor(
        url: String,
        user: String,
        password: String,
        tablePrefix: String,
        maxConnections: Int = 10,
        transactionIsolation: String = "TRANSACTION_REPEATABLE_READ"
    ) : DatabaseConfig(url, user, password, tablePrefix, maxConnections, transactionIsolation)

    /**
     * @param maxConnections Defaults to 10.
     * @param transactionIsolation Defaults to "TRANSACTION_REPEATABLE_READ"
     */
    class Mysql @JvmOverloads constructor(
        url: String,
        user: String,
        password: String,
        tablePrefix: String,
        maxConnections: Int = 10,
        transactionIsolation: String = "TRANSACTION_REPEATABLE_READ"
    ) : DatabaseConfig(url, user, password, tablePrefix, maxConnections, transactionIsolation)

    /**
     * @param maxConnections Defaults to 10.
     * @param transactionIsolation Defaults to "TRANSACTION_REPEATABLE_READ"
     * @param clearDatabase Used to clear the H2 database upon connection. Used for testing.
     */
    class H2 @JvmOverloads constructor(
        url: String,
        tablePrefix: String,
        maxConnections: Int = 10,
        transactionIsolation: String = "TRANSACTION_REPEATABLE_READ",
        val clearDatabase: Boolean = false
    ) : DatabaseConfig(url, "", "", tablePrefix, maxConnections, transactionIsolation)
}
