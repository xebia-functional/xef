package com.xebia.functional.xef.conversation.mlflow

import com.xebia.functional.xef.mlflow.MlflowClient
import com.xebia.functional.xef.mlflow.MlflowClient.*

suspend fun main() {

    val gatewayUri = "http://localhost:5000"

    val client = MlflowClient(gatewayUri)

    val routes = client.searchRoutes()

    println("""
       |######### Routes found ######### 
       |${routes.joinToString(separator = "\n") { printRoute(it) }}
    """.trimMargin())

    val gptRoute = client.getRoute("chat")
    println("Route found: ${gptRoute?.name}")

    val response = gptRoute?.name?.let {route ->
        client.chat(
            route,
            listOf(
                ChatMessage(ChatRole.SYSTEM, "You are a helpful assistant. Be concise"),
                ChatMessage(ChatRole.USER, "What's the best day of the week and why?"),
            ),
            temperature = 0.7,
            maxTokens = 200
        )
    }

    val chatResponse = response?.candidates?.get(0)?.message?.content

    println("Chat GPT response was: \n\n$chatResponse")
}