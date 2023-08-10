package com.xebia.functional.xef.reasoning.pdf

import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.reasoning.tools.Tool

class PDF
@JvmOverloads
constructor(
  chat: Chat,
  model: ChatWithFunctions,
  scope: Conversation,
  @JvmField val readPDFFromFile: ReadPDFFromFile = ReadPDFFromFile(chat, model, scope),
  @JvmField val readPDFFromUrl: ReadPDFFromUrl = ReadPDFFromUrl(chat, model, scope),
) {
  val tools: List<Tool> =
    listOf(
      readPDFFromFile,
      readPDFFromUrl,
    )

  companion object {
    @JvmStatic
    fun create(
      chat: Chat,
      model: ChatWithFunctions,
      scope: Conversation,
    ): PDF = PDF(chat, model, scope)
  }
}
