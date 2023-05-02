package com.xebia.functional.llm.openai

import cats.effect.Sync

import com.xebia.functional.config.OpenAIConfig
import com.xebia.functional.llm.LLM
import com.xebia.functional.llm.models.*
import com.xebia.functional.llm.openai.models.*

trait OpenAIClient[F[_]]:

  val config: OpenAIConfig

  def generate(request: OpenAIRequest): F[List[LLMResult]]

  def createEmbeddings(request: EmbeddingRequest): F[EmbeddingResult]

object OpenAIClient:
  def apply[F[_]: Sync](config: OpenAIConfig): OpenAIClient[F] =
    new OpenAIClientInterpreter[F](config)
