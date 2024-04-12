package com.xebia.functional.xef.server.services

import com.xebia.functional.xef.server.db.tables.User
import com.xebia.functional.xef.server.db.tables.UsersTable
import com.xebia.functional.xef.server.models.LoginRequest
import com.xebia.functional.xef.server.models.LoginResponse
import com.xebia.functional.xef.server.models.RegisterRequest
import com.xebia.functional.xef.server.models.exceptions.XefExceptions.UserException
import com.xebia.functional.xef.server.utils.HashUtils
import io.github.oshai.kotlinlogging.KLogger
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepositoryService(private val logger: KLogger) {

  fun register(request: RegisterRequest): LoginResponse {
    logger.info { "Registering user ${request.email}" }

    return transaction {
      if (User.find { UsersTable.email eq request.email }.count() > 0) {
        throw UserException("User already exists")
      }

      val newSalt = HashUtils.generateSalt()
      val passwordHashed = HashUtils.createHash(request.password, newSalt)
      val user = transaction {
        User.new {
          name = request.name
          email = request.email
          passwordHash = passwordHashed
          salt = newSalt
          authToken = UUID.generateUUID(passwordHashed).toString()
        }
      }
      LoginResponse(user.authToken)
    }
  }

  fun login(request: LoginRequest): LoginResponse {
    logger.info { "Login user ${request.email}" }
    return transaction {
      val user =
        User.find { UsersTable.email eq request.email }.firstOrNull()
          ?: throw UserException("User not found")

      if (!HashUtils.checkPassword(request.password, user.salt, user.passwordHash))
        throw Exception("Invalid password")

      LoginResponse(user.authToken)
    }
  }
}
