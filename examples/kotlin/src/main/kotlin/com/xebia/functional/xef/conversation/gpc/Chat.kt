package com.xebia.functional.xef.conversation.gpc

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.gcp.GcpChat
import com.xebia.functional.xef.gcp.GcpConfig
import com.xebia.functional.xef.prompt.Prompt

suspend fun main() {
  OpenAI.conversation {
    val token = getenv("GCP_TOKEN") ?: error("missing gcp token")
    val gcp =
      GcpChat("codechat-bison@001", GcpConfig(token, "xefdemo", "us-central1")).let(::autoClose)
    while (true) {
      print("\n🤖 Enter your question: ")
      val userInput = readlnOrNull() ?: break
      val answer = gcp.promptMessage(Prompt(userInput))
      println("\n🤖 $answer")
    }
    println("\n🤖 Done")
  }
}
