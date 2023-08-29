package com.xebia.functional.xef.auto.gpc

import com.xebia.functional.gpt4all.conversation
import com.xebia.functional.xef.auto.autoClose
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.gcp.GcpChat
import com.xebia.functional.xef.gcp.GcpConfig

suspend fun main() {
  conversation {
    val token = getenv("GCP_TOKEN") ?: error("missing gcp token")
    val gcp = GcpChat("codechat-bison@001", GcpConfig(token, "xefdemo", "us-central1"))
      .let(::autoClose)
    while (true) {
      print("\n🤖 Enter your question: ")
      val userInput = readlnOrNull() ?: break
      val answer = gcp.promptMessage(userInput)
      println("\n🤖 $answer")
    }
    println("\n🤖 Done")
  }
}
