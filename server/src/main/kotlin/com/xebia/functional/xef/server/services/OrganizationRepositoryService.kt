package com.xebia.functional.xef.server.services

import com.xebia.functional.xef.server.db.tables.Organization
import com.xebia.functional.xef.server.db.tables.OrganizationsTable
import com.xebia.functional.xef.server.db.tables.User
import com.xebia.functional.xef.server.db.tables.UsersTable
import com.xebia.functional.xef.server.models.*
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger

class OrganizationRepositoryService(
    private val logger: Logger
) {
    fun createOrganization(
        data: OrganizationRequest,
        token: String?
    ): OrganizationSimpleResponse {
        logger.info("Creating organization with name: ${data.name}")
        return if (token == null) {
            throw Exception("Token is null")
        } else {
            transaction {
                // Getting the user from the token
                val user = getUser(token)

                // Creating the organization
                val organization = Organization.new {
                    name = data.name
                    ownerId = user.id
                }
                // Adding the organization to the user
                user.organizations = SizedCollection(user.organizations + organization)
                organization.users = SizedCollection(organization.users + user)
                OrganizationSimpleResponse(organization.name)
            }
        }
    }

    fun getOrganizations(token: String?): List<OrganizationWithIdResponse> {
        logger.info("Getting organizations")
        return if (token == null) {
            throw Exception("Token is null")
        } else {
            transaction {
                // Getting the user from the token
                val user = getUser(token)

                // Getting the organizations from the user
                user.organizations.map { OrganizationWithIdResponse(it.id.value, it.name) }
            }
        }
    }

    fun getOrganization(token: String?, id: Int): List<OrganizationFullResponse> {
        logger.info("Getting organizations")
        return if (token == null) {
            throw Exception("Token is null")
        } else {
            transaction {
                // Getting the user from the token
                val user = getUser(token)

                // Getting the organizations from the user
                user.organizations.filter {
                    it.id.value == id
                }.map { OrganizationFullResponse(it.id.value, it.name, it.ownerId.value) }
            }
        }
    }

    fun updateOrganization(
        data: OrganizationUpdateRequest,
        token: String?
    ): OrganizationFullResponse {
        logger.info("Updating organization with name: ${data.name}")
        return if (token == null) {
            throw Exception("Token is null")
        } else {
            transaction {
                // Getting the user from the token
                val user = getUser(token)

                val organization = Organization.find {
                    OrganizationsTable.id eq data.id
                    OrganizationsTable.ownerId eq user.id
                }.firstOrNull()
                    ?: throw Exception("Organization not found")

                // Updating the organization
                organization.name = data.name
                if (data.owner != null) {
                    val newOwner = User.findById(data.owner)
                        ?: throw Exception("User not found")
                    organization.ownerId = newOwner.id
                }
                OrganizationFullResponse(organization.id.value, organization.name, organization.ownerId.value)
            }
        }
    }

    private fun getUser(token: String) =
        User.find { UsersTable.authToken eq token }.firstOrNull() ?: throw Exception("User not found")
}
