package com.xebia.functional.xef.server.services

import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.closeable
import com.xebia.functional.xef.server.db.tables.User
import com.xebia.functional.xef.server.db.tables.UsersTable
import com.xebia.functional.xef.server.models.Token
import com.xebia.functional.xef.server.models.exceptions.XefExceptions
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

suspend fun ResourceScope.hikariDataSource(
  url: String,
  usr: String,
  passwd: String
): HikariDataSource {
  val hikariConfig =
    HikariConfig().apply {
      jdbcUrl = url
      username = usr
      password = passwd
      driverClassName = "org.postgresql.Driver"
    }

  return closeable { HikariDataSource(hikariConfig) }
}

fun Token.getUser() =
  User.find { UsersTable.authToken eq this@getUser.value }.firstOrNull()
    ?: throw XefExceptions.UserException("User not found")
