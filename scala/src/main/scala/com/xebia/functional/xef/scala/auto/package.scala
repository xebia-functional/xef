package com.xebia.functional.xef.scala

import com.xebia.functional.loom.LoomAdapter
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.llm.openai.LLMModel
import io.circe.Decoder
import io.circe.parser.parse
import com.xebia.functional.xef.auto.{AIException, AIKt, Agent as KtAgent}
import com.xebia.functional.xef.pdf.PDFLoaderKt
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.scala.textsplitters.TextSplitter

import java.io.File
import scala.jdk.CollectionConverters.*
import scala.util.*

package object auto {

  def ai[A](block: AIScope ?=> A): A =
    LoomAdapter.apply { cont =>
      AIKt.AIScope[A](
        { (coreAIScope, _) =>
          given AIScope = AIScope.fromCore(coreAIScope)

          block
        },
        (e: AIError, _) => throw AIException(e.getReason),
        cont
      )
    }

  extension [A](block: AIScope ?=> A) {
    inline def getOrElse(orElse: Throwable => A): A =
      Try(ai(block)).fold(orElse, v => v)
  }

  def prompt[A: Decoder: ScalaSerialDescriptor](
      prompt: String,
      maxAttempts: Int = 5,
      llmModel: LLMModel = LLMModel.getGPT_3_5_TURBO,
      user: String = "testing",
      echo: Boolean = false,
      n: Int = 1,
      temperature: Double = 0.0,
      bringFromContext: Int = 10,
      minResponseTokens: Int = 500
  )(using scope: AIScope): A =
    LoomAdapter.apply((cont) =>
      KtAgent.promptWithSerializer[A](
        scope.kt,
        prompt,
        ScalaSerialDescriptor[A].serialDescriptor,
        (json: String) => parse(json).flatMap(Decoder[A].decodeJson(_)).fold(throw _, identity),
        maxAttempts,
        llmModel,
        user,
        echo,
        n,
        temperature,
        bringFromContext,
        minResponseTokens,
        cont
      )
    )

  def contextScope[A: Decoder: ScalaSerialDescriptor](docs: List[String])(block: AIScope ?=> A)(using scope: AIScope): A =
    LoomAdapter.apply(scope.kt.contextScopeWithDocs[A](docs.asJava, (_, _) => block, _))

  def promptMessage(
      prompt: String,
      llmModel: LLMModel = LLMModel.getGPT_3_5_TURBO,
      user: String = "testing",
      echo: Boolean = false,
      n: Int = 1,
      temperature: Double = 0.0,
      bringFromContext: Int = 10,
      minResponseTokens: Int = 500
  )(using scope: AIScope): List[String] =
    LoomAdapter
      .apply[java.util.List[String]](
        KtAgent.promptMessage(scope.kt, prompt, llmModel, user, echo, n, temperature, bringFromContext, minResponseTokens, _)
      ).asScala.toList

  def pdf(
      resource: String | File,
      splitter: TextSplitter = TextSplitter.tokenTextSplitter(ModelType.GPT_3_5_TURBO, 100, 50)
  )(using scope: AIScope): List[String] =
    LoomAdapter
      .apply[java.util.List[String]](count =>
        resource match
          case url: String => PDFLoaderKt.pdf(url, splitter.core, count)
          case file: File => PDFLoaderKt.pdf(file, splitter.core, count)
      ).asScala.toList

  def image[A: Decoder: ScalaSerialDescriptor](
      prompt: String,
      maxAttempts: Int = 5,
      user: String = "testing",
      size: String = "1024x1024",
      bringFromContext: Int = 10,
      llmModel: LLMModel = LLMModel.getGPT_3_5_TURBO,
      echo: Boolean = false,
      n: Int = 1,
      temperature: Double = 0.0,
      minResponseTokens: Int = 500
  )(using scope: AIScope): A =
    LoomAdapter.apply(cont =>
      KtAgent.imageWithSerializer[A](
        scope.kt,
        prompt,
        ScalaSerialDescriptor[A].serialDescriptor,
        (json: String) => parse(json).flatMap(Decoder[A].decodeJson(_)).fold(throw _, identity),
        maxAttempts,
        user,
        size,
        bringFromContext,
        llmModel,
        echo,
        n,
        temperature,
        minResponseTokens,
        cont
      )
    )
}
