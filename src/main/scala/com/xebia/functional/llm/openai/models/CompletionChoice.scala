package com.xebia.functional.llm.openai.models

import com.theokanning.openai.completion.{CompletionChoice => JCompletionChoice}

final case class CompletionChoice(
    text: String,
    index: Integer,
    finishReason: String
)

object CompletionChoice:
  def fromJava(j: JCompletionChoice): CompletionChoice =
    CompletionChoice(j.getText(), j.getIndex(), j.getFinish_reason())
