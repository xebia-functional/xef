package com.xebia.functional.scala.llm

import cats.effect.Sync
import cats.implicits.*

import com.xebia.functional.scala.config.*
import com.xebia.functional.scala.llm.*
import com.xebia.functional.scala.llm.huggingface.*
import com.xebia.functional.scala.llm.models.*
import com.xebia.functional.scala.llm.openai.*

trait LLM[F[_]]:
  def generateFromPrompt(prp: String): F[List[LLMResult]]

object LLM:
  def openAI[F[_]: Sync](c: OpenAIClient[F]): LLM[F] = new LLM[F]:
    def generateFromPrompt(prp: String): F[List[LLMResult]] =
      OpenAIRequest.fromPrompt(prp, c.config).flatMap(c.generate(_))

  def huggingFace[F[_]: Sync](c: HuggingFaceClient[F]): LLM[F] = new LLM[F]:
    def generateFromPrompt(prp: String): F[List[LLMResult]] =
      HFRequest.fromPrompt(prp, c.config).flatMap(c.generate(_))
