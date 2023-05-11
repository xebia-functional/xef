package com.xebia.functional.scala.chains

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.xebia.functional.chains as KtChains
import com.xebia.functional.scala.chains.models.Config
import com.xebia.functional.scala.config.OpenAIConfig
import com.xebia.functional.scala.config.*
import com.xebia.functional.scala.kotlin.CoroutineToIO
import com.xebia.functional.scala.kotlin.CoroutineToIO.given
import com.xebia.functional.scala.llm.*
import com.xebia.functional.scala.llm.models.OpenAIRequest
import com.xebia.functional.scala.llm.models.*
import com.xebia.functional.scala.llm.openai.OpenAIClient
import com.xebia.functional.scala.prompt.PromptTemplate
import eu.timepit.refined.types.string.NonEmptyString
import kotlin.coroutines.Continuation

class LLMChain[F[_]: Async] private (chain: KtChains.Chain) extends Chain[F](chain)

object LLMChain:
  def make[F[_]: Async](
      llm: LLM[F],
      promptTemplate: PromptTemplate[F],
      llmModel: String = "gpt-3.5-turbo",
      user: String = "testing",
      n: Int = 1,
      temperature: Double = 0.0,
      outputVariable: NonEmptyString,
      onlyOutput: Boolean
  ): F[LLMChain[F]] =
      val chainOutput = if onlyOutput
        then KtChains.Chain.ChainOutput.OnlyOutput
        else KtChains.Chain.ChainOutput.InputAndOutput

      val chain: Continuation[? >: KtChains.Chain] => ? =
        KtChains
        .LLMChainKt
        .llmChain(???, ???, llmModel, user, n, temperature, outputVariable.value, chainOutput, _)

      chain.map(new LLMChain[F](_))
