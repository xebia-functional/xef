package com.xebia.functional.xef.server.db.tables

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp


object UsersTable : IntIdTable("users") {
    val name = varchar("name", 20)
    val email = varchar("email", 50)
    val passwordHash = binary("password_hash")
    val salt = binary("salt")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp())
    val authToken = varchar("auth_token", 128)
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(UsersTable)

    var name by UsersTable.name
    var email by UsersTable.email
    var passwordHash by UsersTable.passwordHash
    var salt by UsersTable.salt
    var createdAt by UsersTable.createdAt
    var updatedAt by UsersTable.updatedAt
    var authToken by UsersTable.authToken

    var organizations by Organization via UsersOrgsTable
}
