package com.xebia.functional.xef.aws.bedrock

import arrow.continuations.SuspendApp
import arrow.fx.coroutines.resourceScope
import aws.smithy.kotlin.runtime.client.LogMode
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.aws.bedrock.conf.loadEnvironment
import com.xebia.functional.xef.prompt.ToolCallStrategy
import kotlinx.coroutines.flow.Flow

/**
 * This is an example of how to use the OpenAI API with the Bedrock SDK runtime. Requires the
 * following environment variables to be set:
 * - AWS_ACCESS_KEY_ID
 * - AWS_SECRET_ACCESS_KEY
 * - AWS_REGION_NAME
 */
suspend fun main() = SuspendApp {
  resourceScope {
    val environment = loadEnvironment()
    val runtimeClient =
      sdkClient(
        awsEnv = environment.aws,
        logMode = LogMode.LogResponseWithBody + LogMode.LogRequestWithBody
      )
    val bedrockClient = SdkBedrockClient(runtimeClient)
    val chat = AnthropicBedrockChat(bedrockClient)
    val planet = chat.claude3<Planet>(prompt = "The planet Mars")
    println("planet: $planet")
    val essayStream =
      chat.claude3<Flow<String>>(
        prompt = "Write a critique about your less favorite planet: ${planet.name}"
      )
    val essay = StringBuilder()
    essayStream.collect {
      print(it)
      essay.append(it)
    }
    val sentiment =
      chat.claude3<SentimentEvaluation>(prompt = "$essay\n\nWhat is the sentiment of the essay?")
    println()
    println("sentiment: ${sentiment.evaluation}")
  }
}

private suspend inline fun <reified A> AnthropicBedrockChat.claude3(prompt: String): A =
  AI(
    prompt = prompt,
    api = this,
    model = CreateChatCompletionRequestModel.Custom("anthropic.claude-3-sonnet-20240229-v1:0"),
    toolCallStrategy = ToolCallStrategy.InferXmlFromStringResponse
  )
