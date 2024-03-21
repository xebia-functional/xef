package com.xebia.functional.xef.evaluator.models

import com.xebia.functional.xef.AI
import kotlinx.serialization.Serializable

@Serializable
data class EvaluateResults<E>(val description: String, val items: List<OutputResult<E>>) where
E : AI.PromptClassifier,
E : Enum<E>

@Serializable
data class OutputResult<E>(
  val description: String,
  val contextDescription: String,
  val output: String,
  val result: E
) where E : AI.PromptClassifier, E : Enum<E>
