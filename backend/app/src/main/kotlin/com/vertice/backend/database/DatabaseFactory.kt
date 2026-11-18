package com.vertice.backend.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import javax.sql.DataSource

object DatabaseFactory {
    fun connect(
        jdbcUrl: String,
        username: String,
        password: String,
        maximumPoolSize: Int,
    ): HikariDataSource {
        val hikariConfig = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = username
            this.password = password
            this.maximumPoolSize = maximumPoolSize
            minimumIdle = 1
            connectionTimeout = 10_000
            validationTimeout = 5_000
            maxLifetime = 1_500_000
            dataSourceProperties["tcpKeepAlive"] = "true"
            poolName = "vertice-postgres"
        }

        val dataSource = HikariDataSource(hikariConfig)
        try {
            Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .validateMigrationNaming(true)
                .load()
                .migrate()
        } catch (failure: Throwable) {
            dataSource.close()
            throw failure
        }
        return dataSource
    }
}
