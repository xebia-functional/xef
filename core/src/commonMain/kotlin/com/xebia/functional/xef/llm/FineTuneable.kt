package com.xebia.functional.xef.llm

interface FineTuneable : LLM {

  /** Creates a copy of this instance of [LLM] only changing the models [name]. */
  fun fineTuned(name: String): LLM
}
