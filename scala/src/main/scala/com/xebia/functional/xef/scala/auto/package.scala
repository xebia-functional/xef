package com.xebia.functional.xef.scala.auto

import com.xebia.functional.loom.LoomAdapter
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.Images
import com.xebia.functional.xef.llm.models.functions.CFunction
import io.circe.Decoder
import io.circe.parser.parse
import com.xebia.functional.xef.llm.models.functions.Json
import com.xebia.functional.xef.pdf.Loader
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm._
import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.auto.llm.openai._
import com.xebia.functional.xef.scala.textsplitters.TextSplitter
import com.xebia.functional.xef.llm.models.images.*
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.OpenAIRuntime

import java.io.File
import scala.jdk.CollectionConverters.*
import scala.util.*

type AI[A] = AIScope ?=> A

def ai[A](block: AI[A]): A =
  LoomAdapter.apply { cont =>
    OpenAIRuntime.AIScope[A](
      { (coreAIScope, _) =>
        given AIScope = AIScope.fromCore(coreAIScope)

        block
      },
      (e: AIError, _) => throw e,
      cont
    )
  }

extension [A](block: AI[A]) {
  inline def getOrElse(orElse: Throwable => A): A =
    Try(ai(block)).fold(orElse, v => v)
}

def prompt[A: Decoder: SerialDescriptor](
    prompt: String,
    llmModel: ChatWithFunctions = OpenAI.DEFAULT_SERIALIZATION,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
)(using scope: AIScope): A =
  LoomAdapter.apply((cont) =>
    scope.kt.promptWithSerializer[A](
      llmModel,
      prompt,
      generateCFunctions.asJava,
      (json: String) => parse(json).flatMap(Decoder[A].decodeJson(_)).fold(throw _, identity),
      promptConfiguration,
      cont
    )
  )

private def generateCFunctions[A: SerialDescriptor]: List[CFunction] =
  val descriptor = SerialDescriptor[A].serialDescriptor
  val serialName = descriptor.getSerialName
  val fnName =
    if (serialName.contains(".")) serialName.substring(serialName.lastIndexOf("."), serialName.length)
    else serialName
  List(CFunction(fnName, "Generated function for $fnName", Json.encodeJsonSchema(descriptor)))

def contextScope[A: Decoder: SerialDescriptor](docs: List[String])(block: AI[A])(using scope: AIScope): A =
  LoomAdapter.apply(scope.kt.contextScopeWithDocs[A](docs.asJava, (_, _) => block, _))

def promptMessage(
    prompt: String,
    llmModel: Chat = OpenAI.DEFAULT_CHAT,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
)(using scope: AIScope): String =
  LoomAdapter
    .apply[String](
      scope.kt.promptMessage(llmModel, prompt, promptConfiguration, _)
    )

def promptMessages(
    prompt: String,
    llmModel: Chat = OpenAI.DEFAULT_CHAT,
    functions: List[CFunction] = List.empty,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
)(using scope: AIScope): List[String] =
  LoomAdapter
    .apply[java.util.List[String]](
      scope.kt.promptMessages(llmModel, prompt, functions.asJava, promptConfiguration, _)
    ).asScala.toList

def pdf(
    resource: String | File,
    splitter: TextSplitter = TextSplitter.tokenTextSplitter(ModelType.getDEFAULT_SPLITTER_MODEL, 100, 50)
): List[String] =
  LoomAdapter
    .apply[java.util.List[String]](count =>
      resource match
        case url: String => Loader.pdf(url, splitter.core, count)
        case file: File => Loader.pdf(file, splitter.core, count)
    ).asScala.toList

def images(
    prompt: String,
    model: Images = OpenAI.DEFAULT_IMAGES,
    n: Int = 1,
    size: String = "1024x1024",
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS
)(using scope: AIScope): List[String] =
  LoomAdapter
    .apply[ImagesGenerationResponse](cont =>
      scope.kt.images(
        model,
        prompt,
        n,
        size,
        promptConfiguration,
        cont
      )
    ).getData.asScala.map(_.getUrl).toList
