package com.xebia.functional.xef.server

import arrow.continuations.SuspendApp
import arrow.continuations.ktor.server
import arrow.fx.coroutines.resourceScope
import com.typesafe.config.ConfigFactory
import com.xebia.functional.xef.server.db.psql.Migrate
import com.xebia.functional.xef.server.db.psql.XefDatabaseConfig
import com.xebia.functional.xef.server.exceptions.exceptionsHandler
import com.xebia.functional.xef.server.http.client.ModelUriAdapter
import com.xebia.functional.xef.server.http.client.OpenAIPathType
import com.xebia.functional.xef.server.http.client.mlflow.MLflowModelAdapter
import com.xebia.functional.xef.server.http.routes.*
import com.xebia.functional.xef.server.services.PostgresVectorStoreService
import com.xebia.functional.xef.server.services.RepositoryService
import com.xebia.functional.xef.server.services.VectorStoreService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import kotlinx.coroutines.awaitCancellation
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

object Server {
  @JvmStatic
  fun main(args: Array<String>) = SuspendApp {
    resourceScope {
      val config = ConfigFactory.load("database.conf").resolve()
      val xefDBConfig = XefDatabaseConfig.load("xef", config)
      Migrate.migrate(xefDBConfig)

      val logger = LoggerFactory.getLogger("xef-server")

      val hikariDataSourceXefDB =
        RepositoryService.getHikariDataSource(
          xefDBConfig.getUrl(),
          xefDBConfig.user,
          xefDBConfig.password
        )
      Database.connect(hikariDataSourceXefDB)

      val vectorStoreService =
        VectorStoreService.load("xef-vector-store", config).getVectorStoreService(logger)

      (vectorStoreService as? PostgresVectorStoreService)?.addCollection()

      val ktorClient =
        HttpClient(CIO) {
          engine {
            requestTimeout = 0 // disabled
          }
          install(Auth)
          install(Logging) { level = LogLevel.INFO }
          install(ClientContentNegotiation)
          install(ModelUriAdapter) {
            addToPath(OpenAIPathType.EMBEDDINGS, "ojete/calor" to "https://ca0f47a7-ade7-430a-8735-e1cea32ac960.mock.pstmn.io/http://127.0.0.1:5000/gateway/embeddings/invocations")
          }
          install(MLflowModelAdapter) {
            addToPath("https://ca0f47a7-ade7-430a-8735-e1cea32ac960.mock.pstmn.io/http://127.0.0.1:5000/gateway/embeddings/invocations", OpenAIPathType.EMBEDDINGS)
          }
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
        exceptionsHandler()
        routing {
          xefRoutes(logger)
          aiRoutes(ktorClient)
        }
      }
      awaitCancellation()
    }
  }
}
