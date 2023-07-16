package com.xebia.functional.xef.reasoning.text.events

import com.xebia.functional.xef.auto.Description
import kotlinx.serialization.Serializable

@Serializable
data class EventExtractionResult(
  @Description(["List of events extracted from the text"])
  val events: List<Event>,
)
