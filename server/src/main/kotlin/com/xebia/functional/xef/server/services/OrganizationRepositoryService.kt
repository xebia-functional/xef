package com.xebia.functional.xef.server.services

import com.xebia.functional.xef.server.db.tables.Organization
import com.xebia.functional.xef.server.db.tables.User
import com.xebia.functional.xef.server.db.tables.UsersTable
import com.xebia.functional.xef.server.models.OrganizationRequest
import com.xebia.functional.xef.server.models.OrganizationResponse
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger

class OrganizationRepositoryService(
    private val logger: Logger
) {
    fun createOrganization(
        data: OrganizationRequest,
        token: String?
    ): OrganizationResponse {
        logger.info("Creating organization with name: ${data.name}")
        return if (token == null) {
            throw Exception("Token is null")
        } else {
            transaction {
                // Getting the user from the token
                val user =
                    User.find { UsersTable.authToken eq token }.firstOrNull() ?: throw Exception("User not found")

                // Creating the organization
                val organization = Organization.new {
                    name = data.name
                    ownerId = user.id
                }
                // Adding the organization to the user
                user.organizations = SizedCollection(user.organizations + organization)
                OrganizationResponse(organization.name, message = "Organization created successfully")
            }
        }
    }
}
