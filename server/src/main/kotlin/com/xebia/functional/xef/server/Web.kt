package com.xebia.functional.xef.server

import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*

object WebApp {

    @JvmStatic
    fun main(args: Array<String>) {
        embeddedServer(Netty, port = 8080) {
            routing {
                singlePageApplication {
                    react("web/dist")
                }
            }
        }.start(wait = true)
    }
}