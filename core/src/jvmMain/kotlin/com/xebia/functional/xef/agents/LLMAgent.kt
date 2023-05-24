package com.xebia.functional.xef.agents

import com.xebia.functional.tokenizer.EncodingType
import com.xebia.functional.xef.auto.AIScope
import com.xebia.functional.xef.llm.openai.ChatCompletionRequest
import com.xebia.functional.xef.llm.openai.LLMModel
import com.xebia.functional.xef.llm.openai.Message
import com.xebia.functional.xef.llm.openai.Role
import java.util.regex.Matcher

suspend fun AIScope.patternPrompt(
  prompt: String,
  pattern: Regex,
  model: LLMModel = LLMModel.GPT_3_5_TURBO,
  user: String = "testing",
  n: Int = 1,
  temperature: Double = 0.0,
  maxNewTokens: Int = 30,
  stopAfterMatch: Boolean = true
): String =
  patternPrompt(
    prompt,
    pattern,
    model,
    user,
    n,
    temperature,
    maxNewTokens,
    stopAfterMatch,
    genTokens = 0,
    partialCompletion = "",
  )

private suspend fun AIScope.patternPrompt(
  prompt: String,
  pattern: Regex,
  model: LLMModel,
  user: String,
  n: Int,
  temperature: Double,
  maxNewTokens: Int,
  stopAfterMatch: Boolean,
  genTokens: Int,
  partialCompletion: String
): String {
  if (genTokens >= maxNewTokens) return partialCompletion

  val logitBias: Map<String, Double> =
    TokenFilter(model.modelType.encodingType).buildLogitBias(partialCompletion, pattern)

  val role: String = Role.system.name
  val messages: List<Message> = listOf(Message(role, prompt))

  val request =
    ChatCompletionRequest(
      model = model.name,
      user = user,
      messages = messages,
      n = n,
      temperature = temperature,
      maxTokens = 1,
      logitBias = logitBias
    )

  val outputTokens: List<String> =
    openAIClient.createChatCompletion(request).choices.map { it.message.content }

  val nextPartialCompletion: String = partialCompletion + outputTokens[0]
  val nextPromptPlusCompletion: String = prompt + outputTokens[0]

  if (stopAfterMatch && pattern.matches(nextPartialCompletion)) {
    return nextPartialCompletion
  }

  println(nextPromptPlusCompletion)

  return patternPrompt(
    nextPromptPlusCompletion,
    pattern,
    model,
    user,
    n,
    temperature,
    maxNewTokens,
    stopAfterMatch,
    genTokens = genTokens + 1,
    nextPartialCompletion,
  )
}

interface TokenFilter {
  val tokensCache: Map<Int, String>

  fun buildLogitBias(partialCompletion: String, pattern: Regex): Map<String, Double>

  companion object {
    operator fun invoke(encodingType: EncodingType): TokenFilter =
      object : TokenFilter {
        override val tokensCache: Map<Int, String> = encodingType.buildDecodedTokensCache()

        override fun buildLogitBias(
          partialCompletion: String,
          pattern: Regex
        ): Map<String, Double> = buildMap {
          val openAILimit = 300
          val exclusiveBias = 100.0
          tokensCache
            .asSequence()
            .filter { pattern.partialMatch(partialCompletion + it.value) }
            .take(openAILimit)
            .forEach { put("${it.key}", exclusiveBias) }
        }

        private fun EncodingType.buildDecodedTokensCache(): Map<Int, String> = buildMap {
          base.lineSequence().forEach { line ->
            val (_, rank) = line.split(Regex("\\s+"), limit = 2)
            val tokenId: Int = rank.toInt()
            val token: String = encodingType.encoding.decode(listOf(tokenId))
            put(tokenId, token)
          }
          specialTokensBase.forEach { put(it.value, it.key) }
        }

        private fun Regex.partialMatch(input: String): Boolean {
          val matcher: Matcher = toPattern().matcher(input)
          return if (matcher.matches()) {
            true
          } else {
            matcher.hitEnd()
          }
        }
      }
  }
}
