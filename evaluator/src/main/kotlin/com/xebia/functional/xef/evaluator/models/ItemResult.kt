package com.xebia.functional.xef.evaluator.models

import com.xebia.functional.xef.PromptClassifier
import kotlinx.serialization.Serializable

@Serializable
data class SuiteResults<E>(
  val description: String,
  val model: String,
  val metric: String? = null,
  val items: List<ItemResult<E>>
) where E : PromptClassifier, E : Enum<E>

@Serializable
data class ItemResult<E>(val description: String, val items: List<OutputResult<E>>) where
E : PromptClassifier,
E : Enum<E>

@Serializable
data class OutputResult<E>(
  val description: String,
  val contextDescription: String,
  val output: String,
  val usage: OutputTokens?,
  val result: E,
  val success: Boolean
) where E : PromptClassifier, E : Enum<E>
