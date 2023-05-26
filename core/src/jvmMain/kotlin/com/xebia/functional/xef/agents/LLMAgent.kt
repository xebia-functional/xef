package com.xebia.functional.xef.agents

import com.xebia.functional.tokenizer.TokenVocabulary
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
    tokenVocab = TokenVocabulary(model.modelType.encodingType)
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
  partialCompletion: String,
  tokenVocab: TokenVocabulary
): String {
  if (genTokens >= maxNewTokens) return partialCompletion

  val logitBias: Map<String, Int> = tokenVocab.buildLogitBias(partialCompletion, pattern)

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
    tokenVocab
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

private fun TokenVocabulary.buildLogitBias(
  partialCompletion: String,
  pattern: Regex
): Map<String, Int> = buildMap {
  val openAILimit = 300
  val exclusiveBias = 100
  decodedTokens
    .asSequence()
    .filter { pattern.partialMatch(partialCompletion + it.value) }
    .take(openAILimit)
    .forEach { put("${it.key}", exclusiveBias) }
}

private fun Regex.partialMatch(input: String): Boolean {
  val matcher: Matcher = toPattern().matcher(input)
  return matcher.matches().or(matcher.hitEnd())
}
