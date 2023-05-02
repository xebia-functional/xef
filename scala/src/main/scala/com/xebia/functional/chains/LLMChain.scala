package com.xebia.functional.chains

import cats.effect.Sync
import cats.syntax.all.*

import com.xebia.functional.chains.models.Config
import com.xebia.functional.config.OpenAIConfig
import com.xebia.functional.config.*
import com.xebia.functional.llm.*
import com.xebia.functional.llm.models.OpenAIRequest
import com.xebia.functional.llm.models.*
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.prompt.PromptTemplate
import eu.timepit.refined.types.string.NonEmptyString

class LLMChain[F[_]: Sync](
    llm: LLM[F],
    promptTemplate: PromptTemplate[F],
    outputVariable: NonEmptyString,
    onlyOutput: Boolean
) extends BaseChain[F]:
  val config = Config(promptTemplate.inputKeys.toSet, Set(outputVariable.value), onlyOutput)

  def call(inputs: Map[String, String]): F[Map[String, String]] =
    for
      prp <- preparePrompt(inputs)
      cmp <- llm.generateFromPrompt(prp)
      out = formatOutput(cmp)
    yield out

  def preparePrompt(inputs: Map[String, String]): F[String] = promptTemplate.format(inputs)
  def formatOutput(completions: List[LLMResult]): Map[String, String] =
    Map(outputVariable.value -> completions.map(_.generatedText).mkString(", "))

object LLMChain:
  def make[F[_]: Sync](
      llm: LLM[F],
      promptTemplate: PromptTemplate[F],
      outputVariable: NonEmptyString,
      onlyOutput: Boolean
  ): LLMChain[F] =
    new LLMChain[F](llm, promptTemplate, outputVariable, onlyOutput)
