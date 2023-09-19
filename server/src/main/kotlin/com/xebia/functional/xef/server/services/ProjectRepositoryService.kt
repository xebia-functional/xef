package com.xebia.functional.xef.server.services

import com.xebia.functional.xef.server.db.tables.*
import com.xebia.functional.xef.server.models.*
import com.xebia.functional.xef.server.models.exceptions.XefExceptions.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger

class ProjectRepositoryService(
    private val logger: Logger
) {
    fun createProject(
        data: ProjectRequest,
        token: String
    ): ProjectSimpleResponse {
        logger.info("Creating project with name: ${data.name}")
        return transaction {
            // Getting the user from the token
            val user = getUser(token)

            val organization =
                Organization.findById(data.orgId) ?: throw OrganizationsException("Organization not found")

            if (user.organizations.none { it.id.value == data.orgId }) {
                throw OrganizationsException("User is not part of the organization")
            }

            // Creating the organization
            val project = Project.new {
                name = data.name
                orgId = organization.id
            }
            ProjectSimpleResponse(project.name, project.orgId.value)
        }
    }

    fun getProjects(
        token: String
    ): List<ProjectWithIdResponse> {
        logger.info("Getting projects")
        return transaction {
            // Getting the user from the token
            val user = getUser(token)

            // Getting the organizations from the user
            user.organizations.map { OrganizationWithIdResponse(it.id.value, it.name, it.users.count()) }

            Project.find { ProjectsTable.orgId inList user.organizations.map { it.id } }.mapNotNull { project ->
                val org = user.organizations.find { org -> org.id == project.orgId }
                org?.let {
                    project.toProjectWithIdResponse(it)
                }
            }
        }
    }

    fun getProject(
        token: String,
        id: Int
    ): ProjectWithIdResponse {
        logger.info("Getting project")
        return transaction {
            // Getting the user from the token
            val user = getUser(token)

            val project = Project.findById(id) ?: throw ProjectException("Project not found")

            val org = user.organizations.find { it.id.value == id }
                ?: throw OrganizationsException("User is not part of the organization")

            project.toProjectWithIdResponse(org)
        }
    }

    fun updateProject(
        token: String,
        data: ProjectUpdateRequest,
        id: Int
    ): ProjectWithIdResponse {
        logger.info("Updating project with name: ${data.name}")
        return transaction {
            // Getting the user from the token
            val user = getUser(token)

            val project = Project.findById(id) ?: throw ProjectException("Project not found")

            val organization = Organization.findById(project.orgId)
                ?: throw OrganizationsException("Organization not found")

            if (organization.ownerId != user.id) {
                throw OrganizationsException("You can't update the project. User is not the owner of the organization")
            }

            // Updating the project
            project.name = data.name
            if (data.orgId != null) {
                val newOrg = Organization.findById(data.orgId)
                    ?: throw OrganizationsException("Organization not found")
                project.orgId = newOrg.id
            }
            project.updatedAt = Clock.System.now()
            project.toProjectWithIdResponse(organization)
        }
    }

    fun deleteProject(
        token: String,
        id: Int
    ) {
        logger.info("Deleting project with id: $id")
        transaction {
            val user = getUser(token)
            val project = Project.findById(id)
                ?: throw ProjectException("Project not found")

            val organization = Organization.findById(project.orgId)
                ?: throw OrganizationsException("Organization not found")

            if (organization.ownerId != user.id) {
                throw OrganizationsException("You can't delete the project. User is not the owner of the organization")
            }

            organization.delete()

        }
    }

    private fun getUser(token: String) =
        User.find { UsersTable.authToken eq token }.firstOrNull() ?: throw UserException("User not found")
}
