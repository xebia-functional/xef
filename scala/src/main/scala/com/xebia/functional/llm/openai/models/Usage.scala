package com.xebia.functional.llm.openai.models

import com.theokanning.openai.{Usage => JUsage}

final case class Usage(promptTokens: Long, completionTokens: Long, totalTokens: Long)

object Usage:
  def fromJava(j: JUsage): Usage =
    Usage(j.getPromptTokens(), j.getCompletionTokens(), j.getTotalTokens())
