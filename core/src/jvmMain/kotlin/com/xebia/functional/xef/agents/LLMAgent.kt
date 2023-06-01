package com.xebia.functional.xef.agents

import com.xebia.functional.tokenizer.Encoding
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
  temperature: Double = 0.5,
  maxIterations: Int = 30,
  maxTokensPerCompletion: Int = 1,
  stopAfterMatch: Boolean = true,
  logitBiasMaxSize: Int = 300
): String =
  patternPrompt(
    prompt,
    pattern,
    model,
    user,
    n,
    echo,
    temperature,
    maxIterations,
    maxTokensPerCompletion,
    stopAfterMatch,
    iterations = 0,
    partialCompletion = "",
    tokensVocab = TokenVocabulary(model.modelType.encodingType),
    logitBiasMaxSize = logitBiasMaxSize
  )

private suspend fun AIScope.patternPrompt(
  prompt: String,
  pattern: Regex,
  model: LLMModel,
  user: String,
  n: Int,
  echo: Boolean,
  temperature: Double,
  maxIterations: Int,
  maxTokensPerCompletion: Int = 1,
  stopAfterMatch: Boolean,
  iterations: Int,
  partialCompletion: String,
  tokensVocab: TokenVocabulary,
  logitBiasMaxSize: Int = 300
): String {
  if (iterations >= maxIterations) return partialCompletion

  val patternLogitBias: Map<String, Int> =
    tokensVocab.buildPatternLogitBias(partialCompletion, pattern, maxLength = logitBiasMaxSize)

  val encoding: Encoding = model.modelType.encodingType.encoding

  val logitBias: Map<String, Int> =
    if (patternLogitBias.size < logitBiasMaxSize && maxTokensPerCompletion > 1) {
      buildMap {
        putAll(patternLogitBias)

        patternLogitBias.entries.asSequence().forEach { (key: String) ->
          val token: String = encoding.decode(listOf(key.toInt()))
          val tokenLogitBias: Map<String, Int> =
            tokensVocab.buildPatternLogitBias(
              partialCompletion = partialCompletion + token,
              pattern,
              maxLength = logitBiasMaxSize - size
            )
          putAll(tokenLogitBias)
        }
      }
    } else {
      patternLogitBias
    }

  val outputCompletion: List<String> =
    patternPrompt(model, user, prompt, echo, n, temperature, logitBias, maxTokensPerCompletion)

  val output: String = outputCompletion[0]
  val nextPartialCompletionOutput: String = partialCompletion + output

  val cleanOutput: String =
    nextPartialCompletionOutput
      .removeValuesFromEndUntilRegexMet(tokensVocab.decodedTokens, pattern)
      .replace(partialCompletion, "")
      .ifEmpty { encoding.getLongestMatchingPattern(patternLogitBias) }

  val nextCleanPartialCompletion: String = partialCompletion + cleanOutput
  val nextPromptPlusCompletion: String = prompt + cleanOutput

  if (stopAfterMatch && pattern.matches(nextCleanPartialCompletion)) {
    return nextCleanPartialCompletion
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
    maxIterations,
    maxTokensPerCompletion,
    stopAfterMatch,
    iterations = iterations + 1,
    nextCleanPartialCompletion,
    tokensVocab
  )
}

private suspend fun AIScope.patternPrompt(
  model: LLMModel,
  user: String,
  prompt: String,
  echo: Boolean,
  n: Int,
  temperature: Double,
  logitBias: Map<String, Int>,
  maxTokensPerCompletion: Int = 1,
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
          maxTokens = maxTokensPerCompletion,
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
          maxTokens = maxTokensPerCompletion,
          logitBias = logitBias
        )
      openAIClient.createChatCompletion(request).choices.map { it.message.content }
    }
  }

private fun TokenVocabulary.buildPatternLogitBias(
  partialCompletion: String,
  pattern: Regex,
  maxLength: Int = 300,
  bias: Int = 100
): Map<String, Int> = buildMap {
  decodedTokens
    .asSequence()
    .filter { pattern.partialMatch(partialCompletion + it.value) }
    .take(maxLength)
    .forEach { put("${it.key}", bias) }
}

private fun Regex.partialMatch(input: String): Boolean {
  val matcher: Matcher = toPattern().matcher(input)
  return matcher.matches().or(matcher.hitEnd())
}

private fun String.removeValuesFromEndUntilRegexMet(
  tokens: Map<Int, String>,
  pattern: Regex
): String =
  if (isEmpty() || matchesRegex(tokens, pattern)) { this }
  else { dropLast(n = 1).removeValuesFromEndUntilRegexMet(tokens, pattern) }

private fun Encoding.getLongestMatchingPattern(
  patternLogitBias: Map<String, Int>
): String =
  patternLogitBias.entries.map { (key: String) ->
    decode(listOf(key.toInt()))
  }.maxByOrNull { it.length } ?: ""

private fun String.matchesRegex(tokens: Map<Int, String>, pattern: Regex): Boolean =
  pattern.matches(this) || tokens.asSequence().any { pattern.partialMatch(this + it.value) }
