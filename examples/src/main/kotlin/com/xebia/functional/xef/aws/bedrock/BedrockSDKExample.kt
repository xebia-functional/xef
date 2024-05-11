package com.xebia.functional.xef.aws.bedrock

import arrow.continuations.SuspendApp
import arrow.fx.coroutines.resourceScope
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.aws.bedrock.conf.Environment
import com.xebia.functional.xef.aws.bedrock.conf.Secret
import com.xebia.functional.xef.aws.bedrock.conf.loadEnvironment
import com.xebia.functional.xef.env.getenv

/**
 * This is an example of how to use the OpenAI API with the Bedrock SDK runtime.
 * Requires the following environment variables to be set:
 * - AWS_ACCESS_KEY_ID
 * - AWS_SECRET_ACCESS_KEY
 * - AWS_REGION_NAME
 */
suspend fun main() = SuspendApp {
  resourceScope {
    val environment = loadEnvironment()
    val runtimeClient = sdkClient(environment.aws)
    val bedrockClient = SdkBedrockClient(runtimeClient)
    val chat = BedrockChat(bedrockClient)
    val response = AI<String>(
      prompt = "What is the capital of France?",
      model = CreateChatCompletionRequestModel.Custom("anthropic.claude-3-sonnet-20240229-v1:0"),
      api = chat
    )
    println(response)
  }
}
