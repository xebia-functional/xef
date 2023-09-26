package com.xebia.functional.xef.conversation.llm.openai.models

import com.aallam.openai.api.LegacyOpenAI
import com.aallam.openai.api.completion.Choice
import com.aallam.openai.api.completion.completionRequest
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.conversation.llm.openai.toInternal
import com.xebia.functional.xef.llm.Completion
import com.xebia.functional.xef.llm.FineTunable
import com.xebia.functional.xef.llm.models.text.CompletionChoice
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult

class OpenAICompletion(
  override val modelType: ModelType,
  private val client: OpenAI,
  override val fineTunable: Boolean = false,
) : Completion, FineTunable<OpenAICompletion> {

  override fun spawnFineTunedModel(name: String) =
    OpenAICompletion(ModelType.FineTunedModel(name, modelType), client, fineTunable = false)

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
