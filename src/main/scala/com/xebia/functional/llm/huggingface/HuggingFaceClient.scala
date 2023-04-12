package com.xebia.functional.llm.huggingface

import cats.effect.Concurrent

import com.xebia.functional.config.HuggingFaceConfig
import com.xebia.functional.llm.huggingface.models.*
import org.http4s.Uri
import org.http4s.client.Client

trait HuggingFaceClient[F[_]]:
  def generate(request: InferenceRequest, model: Model): F[List[Generation]]

object HuggingFaceClient:
  def apply[F[_]: Concurrent](config: HuggingFaceConfig, client: Client[F]): HuggingFaceClient[F] =
    new HuggingFaceClientInterpreter[F](config: HuggingFaceConfig, client)
