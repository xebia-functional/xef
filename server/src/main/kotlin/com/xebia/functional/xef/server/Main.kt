package com.xebia.functional.xef.server


import arrow.continuations.SuspendApp
import arrow.fx.coroutines.resourceScope
import arrow.continuations.ktor.server
import com.typesafe.config.ConfigFactory
import com.xebia.functional.xef.server.db.psql.XefDatabaseConfig
import com.xebia.functional.xef.server.db.psql.Migrate
import com.xebia.functional.xef.server.db.psql.XefVectorStoreConfig
import com.xebia.functional.xef.server.db.psql.XefVectorStoreConfig.Companion.getPersistenceService
import com.xebia.functional.xef.server.http.routes.routes
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import kotlinx.coroutines.awaitCancellation

object Main {
    @JvmStatic
    fun main(args: Array<String>) = SuspendApp {
        resourceScope {
            val config = ConfigFactory.load("database.conf").resolve()
            val xefDBConfig = XefDatabaseConfig.load("xef", config)
            Migrate.migrate(xefDBConfig)

            val vectorStoreConfig = XefVectorStoreConfig.load("xef-vector-store", config)
            val persistenceService = vectorStoreConfig.getPersistenceService(config)
            persistenceService.addCollection()

            val ktorClient = HttpClient(CIO){
                install(Auth)
                install(Logging) {
                    level = LogLevel.INFO
                }
                install(ClientContentNegotiation)
            }

            server(factory = Netty, port = 8080, host = "0.0.0.0") {
                install(CORS) {
                    allowNonSimpleContentTypes = true
                    anyHost()
                }
                install(ContentNegotiation) { json() }
                install(Resources)
                install(Authentication) {
                    bearer("auth-bearer") {
                        authenticate { tokenCredential ->
                            UserIdPrincipal(tokenCredential.token)
                        }
                    }
                }
                routing { routes(ktorClient, persistenceService) }
            }
            awaitCancellation()
        }
    }
}
