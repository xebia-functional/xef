package com.xebia.functional.xef.auto.gpc

import com.xebia.functional.gpt4all.conversation
import com.xebia.functional.xef.gcp.pipelines.GcpPipelinesClient

suspend fun main() {
    conversation {
        val pipelineClient = autoClose(GcpPipelinesClient("us-central1", "xef-demo", "token"))
        val answer = pipelineClient.list()
        println("\n🤖 $answer")
    }
}