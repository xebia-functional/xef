package com.xebia.functional.llm.huggingface.models

import io.circe.Decoder
import io.circe.derivation.Configuration
import io.circe.derivation.ConfiguredDecoder

final case class Generation(generatedText: String)
object Generation:
  given Decoder[Generation] =
    ConfiguredDecoder.derived(using Configuration.default.withSnakeCaseMemberNames)
