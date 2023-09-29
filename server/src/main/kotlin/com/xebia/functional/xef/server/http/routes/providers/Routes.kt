package com.xebia.functional.xef.server.http.routes.providers

import com.xebia.functional.xef.server.http.routes.providers.openai.oaiFiles
import com.xebia.functional.xef.server.http.routes.providers.openai.oaiFineTuning
import io.ktor.client.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Routing.oaiRoutes(
    client: HttpClient,
) {
    authenticate("auth-bearer") {
        oaiFineTuning(client)
        oaiFiles(client)
    }
}
