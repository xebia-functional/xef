package com.xebia.functional.xef.auto.gpc

import com.xebia.functional.gpt4all.conversation
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.gcp.GcpConfig
import com.xebia.functional.xef.gcp.pipelines.GcpPipelinesClient

suspend fun main() {
  conversation {
    val token = getenv("GCP_TOKEN") ?: error("missing gcp token")
    val pipelineClient = autoClose(GcpPipelinesClient(GcpConfig(token, "xefdemo", "us-central1")))
    val answer = pipelineClient.list()
    println("\n🤖 $answer")
  }
}
