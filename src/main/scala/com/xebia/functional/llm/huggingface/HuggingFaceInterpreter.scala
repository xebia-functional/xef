package com.xebia.functional.llm.huggingface

import cats.effect.Concurrent
import cats.syntax.all.*

import com.xebia.functional.config.HuggingFaceConfig
import com.xebia.functional.llm.huggingface.models.*
import com.xebia.functional.llm.models.*
import io.circe.Decoder
import org.http4s.EntityDecoder
import org.http4s.Header
import org.http4s.Method
import org.http4s.Request
import org.http4s.Uri
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.client.Client
import org.http4s.headers.*
import org.typelevel.ci.*

class HuggingFaceClientInterpreter[F[_]: Concurrent](val config: HuggingFaceConfig, client: Client[F]) extends HuggingFaceClient[F]:

  def generate(request: HFRequest): F[List[LLMResult]] =
    send[List[LLMResult]](
      baseRequest
        .withMethod(Method.POST)
        .withUri(
          (config.baseUri / "models" / config.model.name)
        ).withEntity(request)
    )

  private def send[A](request: Request[F])(using EntityDecoder[F, A]): F[A] =
    client.expect[A](request).adaptErr { case e: Throwable =>
      HuggingFaceError(Option(e.getMessage()).getOrElse("<null>"))
    }

  private val authHeader = Header.Raw(ci"Authorization", s"Bearer ${config.token}")

  private val baseRequest =
    Request[F]().withHeaders(authHeader)
