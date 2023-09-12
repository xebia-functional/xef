package com.xebia.functional.xef.server.postgresql

import com.xebia.functional.xef.server.db.tables.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer

object TestDatabase {
    private val pgContainer: PostgreSQLContainer<Nothing> =
        PostgreSQLContainer<Nothing>("postgres:alpine3.18").apply {
        withDatabaseName("xefdb")
        withUsername("postgres")
        withPassword("postgres")
        start()
    }

    init {
        val config = HikariConfig().apply {
            jdbcUrl = pgContainer.jdbcUrl.replace("localhost", "0.0.0.0")
            username = pgContainer.username
            password = pgContainer.password
            driverClassName = "org.postgresql.Driver"
        }

        val dataSource = HikariDataSource(config)

        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(UsersTable, OrganizationTable, ProjectsTable, UsersOrgsTable, XefTokensTable)
        }
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class XefDatabaseTest {

    @BeforeAll
    fun setup() {
        TestDatabase
    }

    @Test
    fun crudUser() {
        transaction {
            val newUser = User.new {
                name = "test"
                email = "test@test.com"
                passwordHash = "passwordTest"
                salt = "saltTest"
                authToken = "authTokenTest"
            }

            val retrievedUser = User.findById(newUser.id)
            assertEquals("test", retrievedUser?.name)

            retrievedUser?.apply {
                name = "test2"
            }

            val updatedUser = User.findById(newUser.id)
            assertEquals("test2", updatedUser?.name)

            updatedUser?.delete()
            val deletedUser = User.findById(newUser.id)
            assertNull(deletedUser)
        }
    }

}
