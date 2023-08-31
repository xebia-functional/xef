package com.xebia.functional.xef.conversation.gpc

import com.xebia.functional.gpt4all.conversation
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.gcp.GcpConfig
import com.xebia.functional.xef.gcp.VertexAIRegion
import com.xebia.functional.xef.gcp.pipelines.GcpPipelinesClient

suspend fun main() {
  conversation {
    val token = getenv("GCP_TOKEN") ?: error("missing gcp token")
    val pipelineClient = autoClose(GcpPipelinesClient(GcpConfig(token, "xefdemo", VertexAIRegion.US_CENTRAL1)))
    val answer = pipelineClient.list()
    println("\n🤖 $answer")
  }
}
