package com.xebia.functional.xef.server.postgresql

import com.xebia.functional.xef.server.db.tables.*
import com.xebia.functional.xef.server.models.OpenAIConf
import com.xebia.functional.xef.server.models.ProvidersConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
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
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class XefDatabaseTest {
    @BeforeAll
    fun setup() {
        TestDatabase
    }

    @BeforeEach
    fun cleanup() {
        transaction {
            SchemaUtils.drop(UsersTable, OrganizationTable, ProjectsTable, UsersOrgsTable, XefTokensTable)
            SchemaUtils.create(UsersTable, OrganizationTable, ProjectsTable, UsersOrgsTable, XefTokensTable)
        }
    }

    @Test
    fun crudUser() {
        transaction {
            val newUser = DBHelpers.testUser()

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

    @Test
    fun crudOrganization() {
        transaction {
            val ownerUser = DBHelpers.testUser()
            val newOrganization = DBHelpers.testOrganization(fOwnerId = ownerUser.id)

            val retrievedOrganization = Organization.findById(newOrganization.id)
            assertEquals("testOrg", retrievedOrganization?.name)
            assertEquals(ownerUser.id, retrievedOrganization?.ownerId)

            ownerUser.delete()
            val deletedOrganization = Organization.find { OrganizationTable.name eq newOrganization.name }
            assertEquals(0, deletedOrganization.count())

        }

    }

    @Test
    fun crudProjects() {
        transaction {
            val ownerUser = DBHelpers.testUser()
            val newOrganization = DBHelpers.testOrganization(fOwnerId = ownerUser.id)
            val newProject = DBHelpers.testProject(fOrgId = newOrganization.id)

            val retrievedProject = Project.findById(newProject.id)
            assertEquals(newProject.name, retrievedProject?.name)
            assertEquals(newOrganization.id, retrievedProject?.orgId)
        }
    }

    @Test
    fun organizationsAndUsers() {
        transaction {
            val ownerUser = DBHelpers.testUser()
            val newOrganization = DBHelpers.testOrganization(fOwnerId = ownerUser.id)
            ownerUser.organizations = SizedCollection(listOf(newOrganization))
        }

        transaction {
            val user = User.all().first()
            assertEquals(1, user.organizations.count())
            val newOrganization2 = DBHelpers.testOrganization("testOrg2", fOwnerId = user.id)
            val currentOrganizations = user.organizations
            user.organizations = SizedCollection(currentOrganizations + newOrganization2)
            assertEquals(2, user.organizations.count())
        }
    }

    @Test
    fun crudXefTokens() {
        transaction {
            val user = DBHelpers.testUser()
            val organization = DBHelpers.testOrganization(fOwnerId = user.id)
            user.organizations = SizedCollection(listOf(organization))
            val project = DBHelpers.testProject(fOrgId = organization.id)

            val config = ProvidersConfig(
                openAI = OpenAIConf(
                    name = "dev",
                    token = "testToken",
                    url = "testUrl"
                ),
                gcp = null
            )
            XefTokensTable.insert {
                it[userId] = user.id.value
                it[projectId] = project.id.value
                it[name] = "testEnv"
                it[token] = "testToken"
                it[providersConfig] = config
            }
        }
        transaction {
            val tokens = XefTokensTable.selectAll().map { it.toXefTokens() }
            assertEquals("testToken", tokens[0].token)
        }
    }

}
