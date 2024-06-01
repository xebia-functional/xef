package ai.xef.langchain4j

import ai.xef.Chat
import com.knuddels.jtokkit.api.Encoding
import com.xebia.functional.xef.llm.ChatCompletionRequestMessage
import dev.langchain4j.model.Tokenizer
import kotlin.math.roundToInt

class TokenizerImpl(private val tokenizer: Tokenizer, private val encoding: Encoding) :
  Chat.Tokenizer {
  override fun encode(text: String): List<Int> {
    return encoding.encode(text).boxed()
  }

  override fun truncateText(text: String, maxTokens: Int): String {
    return encoding.truncateText(text, maxTokens)
  }

  private tailrec fun Encoding.truncateText(text: String, maxTokens: Int): String {
    val tokenCount = countTokens(text)
    return if (tokenCount <= maxTokens) text
    else {
      val percentage = maxTokens.toDouble() / tokenCount.toDouble()
      val truncatedTextLength = (text.length * percentage).roundToInt()
      val result = text.substring(0, truncatedTextLength)
      val tokenCountResult = countTokens(result)
      when {
        tokenCountResult >= maxTokens -> truncateText(result, maxTokens)
        else -> result
      }
    }
  }

  override fun tokensFromMessages(history: List<ChatCompletionRequestMessage>): Int =
    //we start with 0 tokens and we accumulate the count of the message content + the tools
    history.fold(0) { acc, message ->
      acc +
        tokenizer.estimateTokenCountInText(message.content) +
        (message.toolCallResults?.let {
          tokenizer.estimateTokenCountInText(it.toolCallId) +
            tokenizer.estimateTokenCountInText(it.toolCallName) +
            tokenizer.estimateTokenCountInText(it.result)
        } ?: 0)
    }
}
