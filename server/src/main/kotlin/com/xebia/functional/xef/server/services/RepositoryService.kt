package com.xebia.functional.xef.server.services

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

object RepositoryService {
    fun getHikariDataSource(
        url: String,
        usr: String,
        passwd: String
    ): HikariDataSource {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = url
            username = usr
            password = passwd
            driverClassName = "org.postgresql.Driver"
        }

        return HikariDataSource(hikariConfig)
    }

}
