package com.xebia.functional.xef.llm

import com.xebia.functional.tokenizer.truncateText
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.MessagesFromHistory
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.store.Memory
import kotlin.math.floor
import kotlin.math.roundToInt

internal object PromptCalculator {

  suspend fun adaptPromptToConversationAndModel(
    prompt: Prompt,
    scope: Conversation,
    llm: BaseChat
  ): Prompt =
    when (prompt.configuration.messagePolicy.addMessagesFromConversation) {
      MessagesFromHistory.ALL -> adaptPromptFromConversation(prompt, scope, llm)
      MessagesFromHistory.NONE -> prompt
    }

  private suspend fun adaptPromptFromConversation(
    prompt: Prompt,
    scope: Conversation,
    llm: BaseChat
  ): Prompt {

    // calculate tokens for history and context
    val remainingTokensForContexts = calculateRemainingTokensForContext(llm, prompt)

    val maxHistoryTokens = calculateMaxHistoryTokens(prompt, remainingTokensForContexts)

    val maxContextTokens = calculateMaxContextTokens(prompt, remainingTokensForContexts)

    // calculate messages for history based on tokens

    val memories: List<Memory> =
      scope.memories(
        llm,
        maxHistoryTokens + prompt.configuration.messagePolicy.historyPaddingTokens
      )

    val historyAllowed = calculateMessagesFromHistory(llm, memories, maxHistoryTokens)

    // calculate messages for context based on tokens
    val ctxInfo =
      scope.store.similaritySearch(
        prompt.messages.joinToString("\n") { it.content },
        prompt.configuration.docsInContext,
      )

    val contextAllowed =
      if (ctxInfo.isNotEmpty()) {
        val ctx: String = ctxInfo.joinToString("\n")

        val ctxTruncated: String = llm.truncateText(ctx, maxContextTokens)

        Prompt { +assistant(ctxTruncated) }.messages
      } else {
        emptyList()
      }

    return prompt.copy(messages = contextAllowed + historyAllowed + prompt.messages)
  }

  private fun messagesFromMemory(memories: List<Memory>): List<Message> =
    memories.map { it.content }

  private fun calculateMessagesFromHistory(
    llm: BaseChat,
    memories: List<Memory>,
    maxHistoryTokens: Int
  ) =
    if (memories.isNotEmpty()) {
      val history = messagesFromMemory(memories)

      // since we have the approximate tokens in memory, we need to fit the messages back to the
      // number of tokens if necessary
      val historyTokens = llm.tokensFromMessages(history)
      if (historyTokens <= maxHistoryTokens) history
      else {
        val historyMessagesWithTokens = history.map { Pair(it, llm.tokensFromMessages(listOf(it))) }

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
    val maxContextTokens = floor(remainingTokensForContexts * (contextPercent / 100f)).roundToInt()
    return maxContextTokens
  }

  private fun calculateMaxHistoryTokens(prompt: Prompt, remainingTokensForContexts: Int): Int {
    val historyPercent = prompt.configuration.messagePolicy.historyPercent
    val maxHistoryTokens = floor(remainingTokensForContexts * (historyPercent / 100f)).roundToInt()
    return maxHistoryTokens
  }

  private fun calculateRemainingTokensForContext(llm: BaseChat, prompt: Prompt): Int {
    val maxContextLength: Int = llm.maxContextLength
    val remainingTokens: Int = maxContextLength - prompt.configuration.minResponseTokens

    val messagesTokens = llm.tokensFromMessages(prompt.messages)

    if (messagesTokens >= remainingTokens) {
      throw AIError.PromptExceedsMaxRemainingTokenLength(messagesTokens, remainingTokens)
    }

    val remainingTokensForContexts = remainingTokens - messagesTokens
    return remainingTokensForContexts
  }

  private suspend fun Conversation.memories(llm: BaseChat, limitTokens: Int): List<Memory> =
    conversationId?.let { store.memories(llm, it, limitTokens) } ?: emptyList()
}
