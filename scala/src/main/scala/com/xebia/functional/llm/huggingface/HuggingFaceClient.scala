package com.xebia.functional.scala.llm.huggingface

import cats.effect.Concurrent

import com.xebia.functional.scala.config.HuggingFaceConfig
import com.xebia.functional.scala.llm.LLM
import com.xebia.functional.scala.llm.huggingface.models.*
import com.xebia.functional.scala.llm.models.*
import org.http4s.Uri
import org.http4s.client.Client

trait HuggingFaceClient[F[_]]:
  val config: HuggingFaceConfig
  def generate(request: HFRequest): F[List[LLMResult]]

object HuggingFaceClient:
  def apply[F[_]: Concurrent](config: HuggingFaceConfig, client: Client[F]): HuggingFaceClient[F] =
    new HuggingFaceClientInterpreter[F](config: HuggingFaceConfig, client)
