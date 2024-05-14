package com.xebia.functional.xef.aws.bedrock.models

import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.openai.generated.api.OpenAI
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.prompt.ToolCallStrategy

/**
 * Amazon Titan Text G1 - Express 1.x amazon.titan-text-express-v1 Amazon Titan Text G1 - Lite 1.x
 * amazon.titan-text-lite-v1 Amazon Titan Text Premier 1.x amazon.titan-text-premier-v1:0 Amazon
 * Titan Embeddings G1 - Text 1.x amazon.titan-embed-text-v1 Amazon Titan Embedding Text v2 1.x
 * amazon.titan-embed-text-v2:0 Amazon Titan Multimodal Embeddings G1 1.x
 * amazon.titan-embed-image-v1 Amazon Titan Image Generator G1 1.x amazon.titan-image-generator-v1
 * Anthropic Claude 2.0 anthropic.claude-v2 Anthropic Claude 2.1 anthropic.claude-v2:1 Anthropic
 * Claude 3 Sonnet 1.0 anthropic.claude-3-sonnet-20240229-v1:0 Anthropic Claude 3 Haiku 1.0
 * anthropic.claude-3-haiku-20240307-v1:0 Anthropic Claude 3 Opus 1.0
 * anthropic.claude-3-opus-20240229-v1:0 Anthropic Claude Instant 1.x anthropic.claude-instant-v1
 * AI21 Labs Jurassic-2 Mid 1.x ai21.j2-mid-v1 AI21 Labs Jurassic-2 Ultra 1.x ai21.j2-ultra-v1
 * Cohere Command 14.x cohere.command-text-v14 Cohere Command Light 15.x
 * cohere.command-light-text-v14 Cohere Command R 1.x cohere.command-r-v1:0 Cohere Command R+ 1.x
 * cohere.command-r-plus-v1:0 Cohere Embed English 3.x cohere.embed-english-v3 Cohere Embed
 * Multilingual 3.x cohere.embed-multilingual-v3 Meta Llama 2 Chat 13B 1.x meta.llama2-13b-chat-v1
 * Meta Llama 2 Chat 70B 1.x meta.llama2-70b-chat-v1 Meta Llama 3 8b Instruct 1.x
 * meta.llama3-8b-instruct-v1:0 Meta Llama 3 70b Instruct 1.x meta.llama3-70b-instruct-v1:0 Mistral
 * AI Mistral 7B Instruct 0.x mistral.mistral-7b-instruct-v0:2 Mistral AI Mixtral 8X7B Instruct 0.x
 * mistral.mixtral-8x7b-instruct-v0:1 Mistral AI Mistral Large 1.x mistral.mistral-large-2402-v1:0
 * Stability AI Stable Diffusion XL 0.x stability.stable-diffusion-xl-v0 Stability AI Stable
 * Diffusion XL 1.x stability.stable-diffusion-xl-v1
 */
object BedrockModel {

  suspend inline fun <reified A> bedrock(
    model: String,
    prompt: String,
    config: Config =
      Config(
        baseUrl = "http://0.0.0.0:4000",
      ),
    openAI: OpenAI = OpenAI(config, logRequests = false),
    api: Chat = openAI.chat
  ): A =
    AI(
      prompt = prompt,
      model = CreateChatCompletionRequestModel.Custom(model),
      api = api,
      toolCallStrategy = ToolCallStrategy.Supported
    )

  object Anthropic {

    suspend inline fun <reified A> claude3(prompt: String): A =
      bedrock("anthropic.claude-3-sonnet-20240229-v1:0", prompt)
  }
}
