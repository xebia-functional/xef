package com.xebia.functional.xef.llm

/**
 * Marks a [LLM] as potentially fine tunable.
 *
 * The generic parameter [T] is needed here, to make the return type of the member functions as
 * specific as possible.
 */
interface FineTunable<T : LLM> : LLM {

  /** If the specific model is actually fine tunable. */
  val fineTunable: Boolean

  /** NOT TO BE CALLED OUTSIDE THIS CLASS, USE [fineTuned] INSTEAD. */
  fun spawnFineTunedModel(name: String): T

  /** Creates a copy of this instance of [LLM] only changing the models [name]. */
  fun fineTuned(name: String): T? = if (fineTunable) spawnFineTunedModel(name) else null
}
