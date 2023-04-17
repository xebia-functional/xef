package com.xebia.functional.chains

import cats.effect.Sync
import cats.syntax.all.*

import com.xebia.functional.chains.models.Config
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.llm.openai.models.CompletionChoice
import com.xebia.functional.llm.openai.models.CompletionRequest
import com.xebia.functional.prompt.PromptTemplate

class LLMChain[F[_]: Sync](
    llm: OpenAIClient[F],
    promptTemplate: PromptTemplate[F],
    llmModel: String,
    user: String,
    echo: Boolean,
    n: Int,
    temperature: Double,
    onlyOutput: Boolean
) extends BaseChain[F]:
  val config = Config(promptTemplate.inputKeys.toSet, Set("answer"), onlyOutput)
  val completionRequest =
    CompletionRequest
      .builder(llmModel, user)
      .withEcho(echo)
      .withN(n)
      .withTemperature(temperature)

  def call(inputs: Map[String, String]): F[Map[String, String]] =
    for
      prp <- preparePrompt(inputs)
      req = completionRequest.withPrompt(prp).build()
      cmp <- llm.createCompletion(req)
      out = formatOutput(cmp)
    yield out

  def run(inputs: Map[String, String] | String): F[Map[String, String]] =
    for
      is <- prepareInputs(inputs)(config)
      os <- call(is)
      output <- prepareOutputs(is, os)(config)
    yield output

  def preparePrompt(inputs: Map[String, String]): F[String] = promptTemplate.format(inputs)
  def formatOutput(completions: List[CompletionChoice]): Map[String, String] =
    config.outputKeys.map((_, completions.map(_.text).mkString(", "))).toMap

object LLMChain:
  def make[F[_]: Sync](
      llm: OpenAIClient[F],
      promptTemplate: PromptTemplate[F],
      llmModel: String,
      user: String,
      echo: Boolean,
      n: Int,
      temperature: Double,
      onlyOutput: Boolean
  ): LLMChain[F] =
    new LLMChain[F](llm: OpenAIClient[F], promptTemplate, llmModel, user, echo, n, temperature, onlyOutput)
