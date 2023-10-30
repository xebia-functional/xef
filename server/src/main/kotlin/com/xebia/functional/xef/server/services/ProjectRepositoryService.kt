package com.xebia.functional.xef.server.services

import com.xebia.functional.xef.server.db.psql.tables.Organization
import com.xebia.functional.xef.server.db.psql.tables.Project
import com.xebia.functional.xef.server.db.psql.tables.ProjectsTable
import com.xebia.functional.xef.server.db.tables.*
import com.xebia.functional.xef.server.models.*
import com.xebia.functional.xef.server.models.exceptions.XefExceptions.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger

class ProjectRepositoryService(private val logger: Logger) {
  fun createProject(data: ProjectRequest, token: Token): ProjectSimpleResponse {
    logger.info("Creating project with name: ${data.name}")
    return transaction {
      val user = token.getUser()

      val organization =
        Organization.findById(data.orgId) ?: throw OrganizationsException("Organization not found")

      if (user.organizations.none { it.id.value == data.orgId }) {
        throw OrganizationsException("User is not part of the organization")
      }

      val project =
        Project.new {
          name = data.name
          orgId = organization.id
        }
      project.toProjectSimpleResponse()
    }
  }

  fun getProjects(token: Token): List<ProjectFullResponse> {
    logger.info("Getting projects")
    return transaction {
      val user = token.getUser()

      Project.find { ProjectsTable.orgId inList user.organizations.map { it.id } }
        .mapNotNull { project ->
          val org = user.organizations.find { org -> org.id == project.orgId }
          org?.let { project.toProjectFullResponse(it) }
        }
    }
  }

  fun getProject(token: Token, id: Int): ProjectFullResponse {
    logger.info("Getting project")
    return transaction {
      val user = token.getUser()

      val project = Project.findById(id) ?: throw ProjectException("Project not found")

      val org =
        user.organizations.find { it.id == project.orgId }
          ?: throw OrganizationsException("User is not part of the organization")

      project.toProjectFullResponse(org)
    }
  }

  fun getProjectsByOrganization(token: Token, orgId: Int): List<ProjectSimpleResponse> {
    logger.info("Getting projects")
    return transaction {
      val user = token.getUser()

      if (user.organizations.none { it.id.value == orgId }) {
        throw OrganizationsException("User is not part of the organization")
      }

      Project.find { ProjectsTable.orgId eq orgId }.map { it.toProjectSimpleResponse() }
    }
  }

  fun updateProject(token: Token, data: ProjectUpdateRequest, id: Int): ProjectFullResponse {
    logger.info("Updating project with name: ${data.name}")
    return transaction {
      val user = token.getUser()

      val project = Project.findById(id) ?: throw ProjectException("Project not found")

      val organization =
        Organization.findById(project.orgId)
          ?: throw OrganizationsException("Organization not found")

      if (organization.ownerId != user.id) {
        throw OrganizationsException(
          "You can't update the project. User is not the owner of the organization"
        )
      }

      // Updating the project
      project.name = data.name
      if (data.orgId != null) {
        val newOrg =
          Organization.findById(data.orgId)
            ?: throw OrganizationsException("Organization not found")
        project.orgId = newOrg.id
      }
      project.updatedAt = Clock.System.now()
      project.toProjectFullResponse(organization)
    }
  }

  fun deleteProject(token: Token, id: Int) {
    logger.info("Deleting project with id: $id")
    transaction {
      val user = token.getUser()
      val project = Project.findById(id) ?: throw ProjectException("Project not found")

      val organization =
        Organization.findById(project.orgId)
          ?: throw OrganizationsException("Organization not found")

      if (organization.ownerId != user.id) {
        throw OrganizationsException(
          "You can't delete the project. User is not the owner of the organization"
        )
      }

      project.delete()
    }
  }
}
