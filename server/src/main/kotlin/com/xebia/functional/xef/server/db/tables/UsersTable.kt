package com.xebia.functional.xef.server.db.tables

import com.xebia.functional.xef.server.models.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp


object UsersTable : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 20)
    val email = varchar("email", 50)
    val passwordHash = varchar("password_hash", 50)
    val salt = varchar("salt", 20)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val authToken = varchar("auth_token", 128)

    override val primaryKey = PrimaryKey(id)
}

fun ResultRow.toUser(): Users {
    return Users(
        id = this[UsersTable.id],
        name = this[UsersTable.name],
        email = this[UsersTable.email],
        password = this[UsersTable.passwordHash],
        salt = this[UsersTable.salt],
        createdAt = this[UsersTable.createdAt],
        updatedAt = this[UsersTable.updatedAt],
        authToken = this[UsersTable.authToken]
    )
}
