package com.xebia.functional.xef.llm

import com.xebia.functional.tokenizer.truncateText
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.store.Memory

internal object PromptCalculator {

  suspend fun adaptPromptToConversationAndModel(
    prompt: Prompt,
    scope: Conversation,
    llm: LLM
  ): Prompt {

    // calculate tokens for history and context
    val remainingTokensForContexts = calculateRemainingTokensForContext(llm, prompt)

    val maxHistoryTokens = calculateMaxHistoryTokens(prompt, remainingTokensForContexts)

    val maxContextTokens = calculateMaxContextTokens(prompt, remainingTokensForContexts)

    // calculate messages for history based on tokens

    val memories: List<Memory> =
      scope.memories(maxHistoryTokens + prompt.configuration.messagePolicy.historyPaddingTokens)

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

        val ctxTruncated: String = llm.modelType.encoding.truncateText(ctx, maxContextTokens)

        Prompt { +assistant(ctxTruncated) }.messages
      } else {
        emptyList()
      }

    return prompt.copy(messages = contextAllowed + historyAllowed + prompt.messages)
  }

  private fun messagesFromMemory(memories: List<Memory>): List<Message> =
    memories.flatMap { it.getSortedMessages() }

  /*
   * Returns the list of messages that fit in the max history tokens
   */
  private fun calculateMessagesFromHistory(
    llm: LLM,
    memories: List<Memory>,
    maxHistoryTokens: Int
  ): List<Message> =
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
            val accPlusMessageTokens = acc.first + pair.second
            if (accPlusMessageTokens > maxHistoryTokens) {
              acc
            } else {
              Pair(accPlusMessageTokens, acc.second + pair.first)
            }
          }
        totalTokenWithMessages.second.reversed()
      }
    } else emptyList()

  /*
   * Calculate the max context tokens based on the context percent
   */
  private fun calculateMaxContextTokens(prompt: Prompt, remainingTokensForContexts: Int): Int {
    val contextPercent = prompt.configuration.messagePolicy.contextPercent
    val maxContextTokens = (remainingTokensForContexts * contextPercent) / 100
    return maxContextTokens
  }

  /*
   * Calculate the max history tokens based on the history percent
   */
  private fun calculateMaxHistoryTokens(prompt: Prompt, remainingTokensForContexts: Int): Int {
    val historyPercent = prompt.configuration.messagePolicy.historyPercent
    val maxHistoryTokens = (remainingTokensForContexts * historyPercent) / 100
    return maxHistoryTokens
  }

  /*
   * Calculate the number of tokens that are available for the context removing the minimum number of tokens for the response
   */
  private fun calculateRemainingTokensForContext(llm: LLM, prompt: Prompt): Int {
    val maxContextLength: Int = llm.modelType.maxContextLength
    val remainingTokens: Int = maxContextLength - prompt.configuration.minResponseTokens

    val messagesTokens = llm.tokensFromMessages(prompt.messages)

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
