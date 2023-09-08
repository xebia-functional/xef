package com.xebia.functional.xef.llm

import com.xebia.functional.tokenizer.truncateText
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.store.Memory

object PromptCalculator {

  suspend fun adaptPromptToConversationAndModel(
    prompt: Prompt,
    scope: Conversation,
    chat: Chat
  ): Prompt {

    // calculate tokens for history and context
    val remainingTokensForContexts = calculateRemainingTokensForContext(chat, prompt)

    val maxHistoryTokens = calculateMaxHistoryTokens(prompt, remainingTokensForContexts)

    val maxContextTokens = calculateMaxContextTokens(prompt, remainingTokensForContexts)

    // calculate messages for history based on tokens

    val memories: List<Memory> =
      scope.memories(maxHistoryTokens + prompt.configuration.messagePolicy.historyPaddingTokens)

    val historyAllowed = calculateMessagesFromHistory(chat, memories, maxHistoryTokens)

    // calculate messages for context based on tokens
    val ctxInfo =
      scope.store.similaritySearch(
        prompt.messages.joinToString("\n") { it.content },
        prompt.configuration.docsInContext,
      )

    val contextAllowed =
      if (ctxInfo.isNotEmpty()) {
        val ctx: String = ctxInfo.joinToString("\n")

        val ctxTruncated: String = chat.modelType.encoding.truncateText(ctx, maxContextTokens)

        Prompt { +assistant(ctxTruncated) }.messages
      } else {
        emptyList()
      }

    return prompt.copy(messages = contextAllowed + historyAllowed + prompt.messages)
  }

  private fun messagesFromMemory(memories: List<Memory>): List<Message> =
    memories.map { it.content }

  private fun calculateMessagesFromHistory(
    chat: Chat,
    memories: List<Memory>,
    maxHistoryTokens: Int
  ) =
    if (memories.isNotEmpty()) {
      val history = messagesFromMemory(memories)

      // since we have the approximate tokens in memory, we need to fit the messages back to the
      // number of tokens if necessary
      val historyTokens = chat.tokensFromMessages(history)
      if (historyTokens <= maxHistoryTokens) history
      else {
        val historyMessagesWithTokens =
          history.map { Pair(it, chat.tokensFromMessages(listOf(it))) }

        val totalTokenWithMessages =
          historyMessagesWithTokens.foldRight(Pair(0, emptyList<Message>())) { pair, acc ->
            if (acc.first + pair.second > maxHistoryTokens) {
              acc
            } else {
              Pair(acc.first + pair.second, acc.second + pair.first)
            }
          }
        totalTokenWithMessages.second.reversed()
      }
    } else emptyList()

  private fun calculateMaxContextTokens(prompt: Prompt, remainingTokensForContexts: Int): Int {
    val contextPercent = prompt.configuration.messagePolicy.contextPercent
    val maxContextTokens = (remainingTokensForContexts * contextPercent) / 100
    return maxContextTokens
  }

  private fun calculateMaxHistoryTokens(prompt: Prompt, remainingTokensForContexts: Int): Int {
    val historyPercent = prompt.configuration.messagePolicy.historyPercent
    val maxHistoryTokens = (remainingTokensForContexts * historyPercent) / 100
    return maxHistoryTokens
  }

  private fun calculateRemainingTokensForContext(chat: Chat, prompt: Prompt): Int {
    val maxContextLength: Int = chat.modelType.maxContextLength
    val remainingTokens: Int = maxContextLength - prompt.configuration.minResponseTokens

    val messagesTokens = chat.tokensFromMessages(prompt.messages)

    if (messagesTokens >= remainingTokens) {
      throw AIError.PromptExceedsMaxRemainingTokenLength(messagesTokens, remainingTokens)
    }

    val remainingTokensForContexts = remainingTokens - messagesTokens
    return remainingTokensForContexts
  }

  private suspend fun Conversation.memories(limitTokens: Int): List<Memory> {
    val cid = conversationId
    return if (cid != null) {
      store.memories(cid, limitTokens)
    } else {
      emptyList()
    }
  }
}
