package com.xebia.functional.xef.server.services

import com.xebia.functional.xef.server.db.psql.tables.Project
import com.xebia.functional.xef.server.db.psql.tables.ProjectsTable
import com.xebia.functional.xef.server.db.psql.tables.XefTokens
import com.xebia.functional.xef.server.db.psql.tables.XefTokensTable
import com.xebia.functional.xef.server.db.tables.*
import com.xebia.functional.xef.server.models.*
import com.xebia.functional.xef.server.models.exceptions.XefExceptions.*
import java.util.UUID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger

class TokenRepositoryService(private val logger: Logger) {
  fun createToken(data: TokenRequest, userToken: Token): TokenSimpleResponse {
    return transaction {
      val user = userToken.getUser()

      val project =
        Project.findById(data.projectId) ?: throw OrganizationsException("Project not found")

      logger.info("Creating token with name ${data.name} from project ${project.name}")

      if (user.organizations.none { it.id == project.orgId }) {
        throw OrganizationsException(
          "User is not part of the organization of the ${project.name} project"
        )
      }

      val newXefToken =
        XefTokens.new {
          name = data.name
          userId = user.id
          projectId = project.id
          token = UUID.randomUUID().toString()
          providersConfig = ProvidersConfig.empty
        }

      newXefToken.toTokenSimpleResponse()
    }
  }

  fun getTokens(userToken: Token): List<TokenFullResponse> {
    logger.info("Getting tokens")
    return transaction {
      val user = userToken.getUser()

      val userProjects =
        Project.find { ProjectsTable.orgId inList user.organizations.map { it.id } }
          .mapNotNull { project ->
            val org = user.organizations.find { org -> org.id == project.orgId }
            org?.let { project.toProjectSimpleResponse() }
          }

      XefTokens.find { XefTokensTable.userId eq user.id }
        .mapNotNull { xefToken ->
          userProjects
            .find { project -> project.id == xefToken.projectId.value }
            ?.let { xefToken.toTokenFullResponse(it) }
        }
    }
  }

  fun getTokensByProject(userToken: Token, projectId: Int): List<TokenFullResponse> {
    logger.info("Getting tokens by project")
    return transaction {
      val user = userToken.getUser()

      XefTokens.find((XefTokensTable.userId eq user.id) and (XefTokensTable.projectId eq projectId))
        .map { xefToken ->
          val project =
            Project.findById(xefToken.projectId) ?: throw ProjectException("Project not found")
          xefToken.toTokenFullResponse(project.toProjectSimpleResponse())
        }
    }
  }

  fun updateToken(userToken: Token, data: TokenUpdateRequest, id: Int): TokenFullResponse {
    logger.info("Updating token with name: ${data.name}")
    return transaction {
      val user = userToken.getUser()

      val xefToken = XefTokens.findById(id) ?: throw XefTokenException("Token not found")

      val project =
        Project.findById(xefToken.projectId) ?: throw ProjectException("Project not found")

      if (user.organizations.none { it.id == project.orgId }) {
        throw OrganizationsException("User is not part of the organization of this project")
      }

      val xefTokenUpdated =
        xefToken.apply {
          name = data.name
          providersConfig = data.providerConfig
        }

      xefTokenUpdated.toTokenFullResponse(project.toProjectSimpleResponse())
    }
  }

  fun deleteToken(userToken: Token, id: Int) {
    logger.info("Deleting token: $id")
    transaction {
      val user = userToken.getUser()

      val xefToken = XefTokens.findById(id) ?: throw XefTokenException("Token not found")

      val project =
        Project.findById(xefToken.projectId) ?: throw ProjectException("Project not found")

      if (user.organizations.none { it.id == project.orgId }) {
        throw OrganizationsException("User is not part of the organization of this project")
      }

      XefTokensTable.deleteWhere { this.id eq id }
    }
  }
}
