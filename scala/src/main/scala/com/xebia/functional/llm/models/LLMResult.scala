package com.xebia.functional.scala.llm.models

import com.theokanning.openai.completion.{CompletionChoice => JCompletionChoice}
import io.circe.Decoder
import io.circe.derivation.Configuration
import io.circe.derivation.ConfiguredDecoder

final case class LLMResult(generatedText: String)

object LLMResult:
  given Decoder[LLMResult] =
    ConfiguredDecoder.derived(using Configuration.default.withSnakeCaseMemberNames)
