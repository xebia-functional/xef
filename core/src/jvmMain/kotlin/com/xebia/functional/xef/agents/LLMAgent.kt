package com.xebia.functional.xef.agents

import com.xebia.functional.tokenizer.EncodingType
import com.xebia.functional.xef.auto.AIScope
import com.xebia.functional.xef.llm.openai.ChatCompletionRequest
import com.xebia.functional.xef.llm.openai.CompletionRequest
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
  echo: Boolean = false,
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
    echo,
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
  echo: Boolean,
  temperature: Double,
  maxNewTokens: Int,
  stopAfterMatch: Boolean,
  genTokens: Int,
  partialCompletion: String
): String {
  if (genTokens >= maxNewTokens) return partialCompletion

  val logitBias: Map<String, Int> =
    TokenFilter(model.modelType.encodingType).buildLogitBias(partialCompletion, pattern)

  val outputCompletion: List<String> =
    patternPrompt(model, user, prompt, echo, n, temperature, logitBias)

  val nextPartialCompletion: String = partialCompletion + outputCompletion[0]
  val nextPromptPlusCompletion: String = prompt + outputCompletion[0]

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
    echo,
    temperature,
    maxNewTokens,
    stopAfterMatch,
    genTokens = genTokens + 1,
    nextPartialCompletion,
  )
}

private suspend fun AIScope.patternPrompt(
  model: LLMModel,
  user: String,
  prompt: String,
  echo: Boolean,
  n: Int,
  temperature: Double,
  logitBias: Map<String, Int>
): List<String> =
  when (model.kind) {
    LLMModel.Kind.Completion -> {
      val request =
        CompletionRequest(
          model = model.name,
          user = user,
          prompt = prompt,
          echo = echo,
          n = n,
          temperature = temperature,
          maxTokens = 1,
          logitBias = logitBias
        )
      openAIClient.createCompletion(request).choices.map { it.text }
    }
    LLMModel.Kind.Chat -> {
      val role: String = Role.system.name
      val request =
        ChatCompletionRequest(
          model = model.name,
          messages = listOf(Message(role, prompt)),
          temperature = temperature,
          n = n,
          user = user,
          maxTokens = 1,
          logitBias = logitBias
        )
      openAIClient.createChatCompletion(request).choices.map { it.message.content }
    }
  }

interface TokenFilter {
  val tokensCache: Map<Int, String>

  fun buildLogitBias(partialCompletion: String, pattern: Regex): Map<String, Int>

  companion object {
    operator fun invoke(encodingType: EncodingType): TokenFilter =
      object : TokenFilter {
        override val tokensCache: Map<Int, String> = encodingType.buildDecodedTokensCache()

        override fun buildLogitBias(partialCompletion: String, pattern: Regex): Map<String, Int> =
          buildMap {
            val openAILimit = 300
            val exclusiveBias = 100
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
          return matcher.matches().or(matcher.hitEnd())
        }
      }
  }
}
