package com.xebia.functional.xef.server.services

import com.xebia.functional.xef.server.db.tables.Organization
import com.xebia.functional.xef.server.db.tables.User
import com.xebia.functional.xef.server.db.tables.UsersTable
import com.xebia.functional.xef.server.models.*
import com.xebia.functional.xef.server.models.exceptions.XefExceptions.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger

class OrganizationRepositoryService(
    private val logger: Logger
) {
    fun createOrganization(
        data: OrganizationRequest,
        token: String
    ): OrganizationSimpleResponse {
        logger.info("Creating organization with name: ${data.name}")
        return transaction {
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
            organization.toOrganizationSimpleResponse()
        }
    }

    fun getOrganizations(
        token: String
    ): List<OrganizationWithIdResponse> {
        logger.info("Getting organizations")
        return transaction {
            // Getting the user from the token
            val user = getUser(token)

            // Getting the organizations from the user
            user.organizations.map { it.toOrganizationWithIdResponse() }
        }
    }

    fun getOrganization(
        token: String,
        id: Int
    ): OrganizationFullResponse {
        logger.info("Getting organizations")
        return transaction {
            // Getting the user from the token
            val user = getUser(token)

            // Getting the organization
            user.organizations.find {
                it.id.value == id
            }?.toOrganizationFullResponse() ?: throw OrganizationsException("Organization not found")
        }
    }

    fun getUsersInOrganization(
        token: String,
        id: Int
    ): List<UserResponse> {
        logger.info("Getting users in organization")
        return transaction {
            // Getting the user from the token
            val user = getUser(token)

            // Getting the organizations from the user
            user.organizations.filter {
                it.id.value == id
            }.flatMap { it.users }.map { it.toUserResponse() }
        }
    }

    fun updateOrganization(
        token: String,
        data: OrganizationUpdateRequest,
        id: Int
    ): OrganizationFullResponse {
        logger.info("Updating organization with name: ${data.name}")
        return transaction {
            // Getting the user from the token
            val user = getUser(token)

            val organization = Organization.findById(id)
                ?: throw OrganizationsException("Organization not found")

            if (organization.ownerId != user.id) {
                throw OrganizationsException("User is not the owner of the organization")
            }

            // Updating the organization
            organization.name = data.name
            if (data.owner != null) {
                val newOwner = User.findById(data.owner)
                    ?: throw UserException("User not found")
                organization.ownerId = newOwner.id
            }
            organization.updatedAt = Clock.System.now()
            organization.toOrganizationFullResponse()
        }
    }

    fun deleteOrganization(
        token: String,
        id: Int
    ) {
        logger.info("Deleting organization with id: $id")
        transaction {
            val user = getUser(token)
            val organization = Organization.findById(id)
                ?: throw OrganizationsException("Organization not found")

            if (organization.ownerId != user.id) {
                throw OrganizationsException("You can't delete the organization. User is not the owner of the organization")
            }

            organization.delete()
        }
    }

    private fun getUser(token: String) =
        User.find { UsersTable.authToken eq token }.firstOrNull() ?: throw UserException("User not found")
}
