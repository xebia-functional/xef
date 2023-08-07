package com.xebia.functional.xef.auto.gpc

import com.xebia.functional.gpt4all.getOrThrow
import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.gcp.pipelines.GcpPipelinesClient

suspend fun main() {
    ai {
        val pipelineClient = autoClose(GcpPipelinesClient("us-central1", "xef-demo", "token"))
        val answer = pipelineClient.list()
        println("\n🤖 $answer")
    }.getOrThrow()
}