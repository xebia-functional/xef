package com.xebia.functional.xef.server.http.routes

import com.xebia.functional.xef.server.services.OrganizationRepositoryService
import com.xebia.functional.xef.server.services.ProjectRepositoryService
import com.xebia.functional.xef.server.services.UserRepositoryService
import io.ktor.client.*
import io.ktor.server.routing.*
import org.slf4j.Logger

fun Routing.xefRoutes(
  logger: Logger
) {
  userRoutes(UserRepositoryService(logger))
  organizationRoutes(OrganizationRepositoryService(logger))
  projectsRoutes(ProjectRepositoryService(logger))
}
