package com.xebia.functional.llm.huggingface.models

import io.circe.Encoder

final case class InferenceRequest(inputs: String, maxLength: Int = 1000) derives Encoder.AsObject
