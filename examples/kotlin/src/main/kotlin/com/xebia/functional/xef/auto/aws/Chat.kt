package com.xebia.functional.xef.auto.aws

import com.xebia.functional.gpt4all.getOrThrow
import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.aws.SageMakerChat
import com.xebia.functional.xef.gcp.GcpChat

suspend fun main() {
  ai {
    /*
    arn:aws:sagemaker:us-east-2:516203532587:endpoint/xef-falcon-7b-endpoint
     */
    val gcp = autoClose(SageMakerChat(region = "us-east-2", endpointName = "xef-falcon-7b-endpoint", ))
    while (true) {
      print("\n🤖 Enter your question: ")
      val userInput = readlnOrNull() ?: break
      val answer = gcp.promptMessage(userInput)
      println("\n🤖 $answer")
    }
    println("\n🤖 Done")
  }.getOrThrow()
}

