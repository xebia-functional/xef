package com.xebia.functional.xef.server


import arrow.continuations.SuspendApp
import arrow.fx.coroutines.resourceScope
import arrow.continuations.ktor.server
import com.xebia.functional.xef.server.http.routes.routes
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
                routing { routes() }
            }
            awaitCancellation()
        }
    }
}
