package com.xebia.functional.xef.conversation.llm.openai.models

import com.aallam.openai.api.LegacyOpenAI
import com.aallam.openai.api.completion.Choice
import com.aallam.openai.api.completion.completionRequest
import com.aallam.openai.api.model.ModelId
import com.xebia.functional.tokenizer.EncodingType
import com.xebia.functional.tokenizer.truncateText
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.toInternal
import com.xebia.functional.xef.llm.Completion
import com.xebia.functional.xef.llm.models.MaxIoContextLength
import com.xebia.functional.xef.llm.models.ModelID
import com.xebia.functional.xef.llm.models.text.CompletionChoice
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult

class OpenAICompletion(
  private val provider: OpenAI, // TODO: use context receiver
  override val modelID: ModelID,
  override val contextLength: MaxIoContextLength,
  override val encodingType: EncodingType,
) : Completion, OpenAIModel {

  private val client = provider.defaultClient

  override fun copy(modelID: ModelID) =
    OpenAICompletion(provider, modelID, contextLength, encodingType)

  override fun countTokens(text: String): Int = encoding.countTokens(text)

  override fun truncateText(text: String, maxTokens: Int): String =
    encoding.truncateText(text, maxTokens)

  @OptIn(LegacyOpenAI::class)
  override suspend fun createCompletion(request: CompletionRequest): CompletionResult {
    fun toInternal(it: Choice): CompletionChoice =
      CompletionChoice(it.text, it.index, null, it.finishReason.value)

    val openAIRequest = completionRequest {
      model = ModelId(request.model)
      user = request.user
      prompt = request.prompt
      suffix = request.suffix
      maxTokens = request.maxTokens
      temperature = request.temperature
      topP = request.topP
      n = request.n
      logprobs = request.logprobs
      echo = request.echo
      stop = request.stop
      presencePenalty = request.presencePenalty
      frequencyPenalty = request.frequencyPenalty
      bestOf = request.bestOf
      logitBias = request.logitBias
    }

    val response = client.completion(openAIRequest)
    return CompletionResult(
      id = response.id,
      `object` = response.model.id,
      created = response.created,
      model = response.model.id,
      choices = response.choices.map { toInternal(it) },
      usage = response.usage.toInternal()
    )
  }
}
