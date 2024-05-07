package com.xebia.functional.xef.server

import arrow.continuations.SuspendApp
import arrow.continuations.ktor.server
import arrow.fx.coroutines.resourceScope
import com.typesafe.config.ConfigFactory
import com.xebia.functional.xef.server.db.psql.XefDatabaseConfig
import com.xebia.functional.xef.server.exceptions.exceptionsHandler
import com.xebia.functional.xef.server.http.routes.aiRoutes
import com.xebia.functional.xef.server.http.routes.xefRoutes
import com.xebia.functional.xef.server.services.hikariDataSource
import com.xebia.functional.xef.server.services.vectorStoreService
import com.xebia.functional.xef.store.migrations.runDatabaseMigrations
import guru.zoroark.tegral.openapi.ktor.TegralOpenApiKtor
import guru.zoroark.tegral.openapi.ktor.openApiEndpoint
import guru.zoroark.tegral.openapi.ktorui.TegralSwaggerUiKtor
import guru.zoroark.tegral.openapi.ktorui.swaggerUiEndpoint
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.resources.Resources
import io.ktor.server.routing.routing
import kotlinx.coroutines.awaitCancellation
import org.jetbrains.exposed.sql.Database

object Server {
  @JvmStatic
  fun main(args: Array<String>) = SuspendApp {
    resourceScope {
      val logger = KotlinLogging.logger("xef-server")

      val config = ConfigFactory.load("database.conf").resolve()
      val xefDBConfig = XefDatabaseConfig.load("xef-database", config)

      val xefDatasource = hikariDataSource(xefDBConfig.url, xefDBConfig.user, xefDBConfig.password)

      runDatabaseMigrations(
        xefDatasource,
        xefDBConfig.migrationsTable,
        xefDBConfig.migrationsLocations
      )

      Database.connect(xefDatasource)

      vectorStoreService("xef-vector-store", config, logger)

      val ktorClient =
        HttpClient(CIO) {
          engine {
            requestTimeout = 0 // disabled
          }
          install(Auth)
          install(Logging) { level = LogLevel.INFO }
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
        install(Resources)
        install(Authentication) {
          bearer("auth-bearer") {
            authenticate { tokenCredential -> UserIdPrincipal(tokenCredential.token) }
          }
        }
        install(TegralOpenApiKtor) { title = "Xef Server" }
        install(TegralSwaggerUiKtor)
        exceptionsHandler()
        routing {
          xefRoutes(logger)
          aiRoutes(ktorClient)
          openApiEndpoint("/openapi")
          swaggerUiEndpoint(path = "/docs", openApiPath = "/openapi")
        }
      }
      awaitCancellation()
    }
  }
}
