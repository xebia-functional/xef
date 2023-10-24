package com.xebia.functional.xef.conversation.mlflow

import com.xebia.functional.xef.mlflow.MlflowClient
import com.xebia.functional.xef.mlflow.MlflowClient.*

suspend fun main() {

  val gatewayUri = "http://localhost:5000"

  val client = MlflowClient(gatewayUri)

  println("MLflow Gateway client created. Press any key to continue...")
  readlnOrNull()

  println("Searching available models...")
  println()
  val routes = client.searchRoutes()

  println(
    """
       |######### Routes found ######### 
       |${routes.joinToString(separator = "\n") { printRoute(it) }}
    """
      .trimMargin()
  )
  println()

  while (true) {

    println("Select the route you want to interact with")
    val route = readlnOrNull() ?: "chat"

    val gptRoute = client.getRoute(route)
    println("Route found: ${gptRoute?.name}. What do you want to ask?")

    val question = readlnOrNull() ?: "What's the best day of the week and why?"

    val response =
      gptRoute?.name?.let { it ->
        client.chat(
          it,
          listOf(
            ChatMessage(ChatRole.SYSTEM, "You are a helpful assistant. Be concise"),
            ChatMessage(ChatRole.USER, question),
          ),
          temperature = 0.7,
          maxTokens = 200
        )
      }

    val chatResponse = response?.candidates?.get(0)?.message?.content

    println("Chat GPT response was: \n\n$chatResponse")
    println()
    println("Do you want to continue? (y/N)")
    val userInput = readlnOrNull() ?: ""
    if (!userInput.equals("y", true)) break
  }
}

private fun printModel(model: RouteModel): String =
  "(name = '${model.name}', provider = '${model.provider}')"

private fun printRoute(r: RouteDefinition): String =
  """
    Name: ${r.name}
    * Route type: ${r.routeType}
    * Route url: ${r.routeUrl}
    * Model: ${printModel(r.model)}
"""
    .trimIndent()
