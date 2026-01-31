package io.nexure.discount.infrastructure.config

import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

object DatabaseConfig {
    private val config = ConfigFactory.load()

    fun init() {
        Database.connect(createHikariDataSource())
    }

    private fun createHikariDataSource(): HikariDataSource {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.getString("database.url")
            driverClassName = config.getString("database.driver")
            username = config.getString("database.user")
            password = config.getString("database.password")
            maximumPoolSize = config.getInt("database.maxPoolSize")
        }
        return HikariDataSource(hikariConfig)
    }
}
