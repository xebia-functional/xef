package com.xebia.functional.xef.conversation.gpc

import arrow.core.nonEmptyListOf
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.gcp.GcpClient
import com.xebia.functional.xef.gcp.GcpConfig
import com.xebia.functional.xef.gcp.promptMessage
import com.xebia.functional.xef.prompt.Prompt

suspend fun main() {
  val token =
    getenv("GCP_TOKEN") ?: throw AIError.Env.GCP(nonEmptyListOf("missing GCP_TOKEN env var"))

  val modelID = "textembedding-gecko"

  val config = GcpConfig(token, "xefdemo", "us-central1")

  GcpClient.conversation(modelID, config) {
    while (true) {
      print("\n🤖 Enter your question: ")
      val userInput = readlnOrNull() ?: break
      val answer = promptMessage(Prompt(userInput))
      println("\n🤖 $answer")
    }
    println("\n🤖 Done")
  }
}
