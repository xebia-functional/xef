package com.xebia.functional.xef.server.http.routes

import com.xebia.functional.xef.server.services.OrganizationRepositoryService
import com.xebia.functional.xef.server.services.ProjectRepositoryService
import com.xebia.functional.xef.server.services.TokenRepositoryService
import com.xebia.functional.xef.server.services.UserRepositoryService
import io.github.oshai.kotlinlogging.KLogger
import io.ktor.server.routing.*

fun Routing.xefRoutes(logger: KLogger) {
  userRoutes(UserRepositoryService(logger))
  organizationRoutes(OrganizationRepositoryService(logger))
  projectsRoutes(ProjectRepositoryService(logger))
  tokensRoutes(TokenRepositoryService(logger))
}
