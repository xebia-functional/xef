package com.xebia.functional.xef.server

import arrow.continuations.SuspendApp
import arrow.continuations.ktor.server
import arrow.fx.coroutines.resourceScope
import com.typesafe.config.ConfigFactory
import com.xebia.functional.xef.server.db.psql.Migrate
import com.xebia.functional.xef.server.db.psql.XefDatabaseConfig
import com.xebia.functional.xef.server.db.psql.XefVectorStoreConfig
import com.xebia.functional.xef.server.db.psql.XefVectorStoreConfig.Companion.getVectorStoreService
import com.xebia.functional.xef.server.exceptions.exceptionsHandler
import com.xebia.functional.xef.server.http.routes.*
import com.xebia.functional.xef.server.services.OrganizationRepositoryService
import com.xebia.functional.xef.server.services.ProjectRepositoryService
import com.xebia.functional.xef.server.services.RepositoryService
import com.xebia.functional.xef.server.services.UserRepositoryService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.awaitCancellation
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation

object Server {
    @JvmStatic
    fun main(args: Array<String>) = SuspendApp {
        resourceScope {
            val config = ConfigFactory.load("database.conf").resolve()
            val xefDBConfig = XefDatabaseConfig.load("xef", config)
            Migrate.migrate(xefDBConfig)

            val logger = LoggerFactory.getLogger("xef-server")

            val hikariDataSourceXefDB = RepositoryService.getHikariDataSource(
                xefDBConfig.getUrl(),
                xefDBConfig.user,
                xefDBConfig.password
            )
            Database.connect(hikariDataSourceXefDB)
            val vectorStoreConfig = XefVectorStoreConfig.load("xef-vector-store", config)
            val vectorStoreService = vectorStoreConfig.getVectorStoreService(config, logger)
            vectorStoreService.addCollection()


            val ktorClient = HttpClient(CIO) {
                engine {
                    requestTimeout = 0 // disabled
                }
                install(Auth)
                install(Logging) {
                    level = LogLevel.INFO
                }
                install(ClientContentNegotiation)
            }

            server(factory = Netty, port = 8081, host = "0.0.0.0") {
                install(CORS) {
                    allowNonSimpleContentTypes = true
                    HttpMethod.DefaultMethods.forEach { allowMethod(it) }
                    allowHeaders { true }
                    anyHost()
                }
                install(ContentNegotiation) { json() }
                install(DoubleReceive)
                install(Resources)
                install(Authentication) {
                    bearer("auth-bearer") {
                        authenticate { tokenCredential ->
                            UserIdPrincipal(tokenCredential.token)
                        }
                    }
                }
                exceptionsHandler()
                routing {
                    genAIRoutes(ktorClient, vectorStoreService)
                    fineTuningRoutes(ktorClient)
                    userRoutes(UserRepositoryService(logger))
                    organizationRoutes(OrganizationRepositoryService(logger))
                    projectsRoutes(ProjectRepositoryService(logger))
                }
            }
            awaitCancellation()
        }
    }
}
