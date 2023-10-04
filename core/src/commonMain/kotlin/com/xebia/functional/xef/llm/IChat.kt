package com.xebia.functional.xef.llm

import com.xebia.functional.xef.llm.LLM
import com.xebia.functional.xef.llm.models.MaxContextLength
import com.xebia.functional.xef.llm.models.chat.Message
import kotlin.math.roundToInt

interface IChat : LLM {
    val contextLength: MaxContextLength
    suspend fun estimateTokens(messages: List<Message>): Int
    suspend fun estimateTokens(rawMessage: String): Int
}

/**
 * Truncates the given [text] to the given [maxTokens] by removing tokens from the end of the text.
 * It removes tokens from the tail of the [text].
 * Tokens are chosen to be removed based on the percentage of the [maxTokens]
 * compared to the total amount of tokens in the [text].
 *
 * If the truncation fails,
 * it will retry by recursively calling this function until a text with maxTokens is found.
 *
 * **WARNING:** for small [maxTokens] this function may hang forever,
 * some [text] like emoticons, or special characters have token length of 9.
 * So trying to truncateText to maxToken = 5 might hang forever for them.
 *
 * **WARNING:** This method might truncate crucial information from your prompt,
 * and as a result might degrade reliability of your prompts.
 */
tailrec suspend fun IChat.truncateText(text: String, maxTokens: Int): String {
    val tokenCount = estimateTokens(text)
    return if (tokenCount <= maxTokens) text
    else {
        val percentage = maxTokens.toDouble() / tokenCount.toDouble()
        val truncatedTextLength = (text.length * percentage).roundToInt()
        val result = text.substring(0, truncatedTextLength)
        val tokenCountResult = estimateTokens(result)
        when {
            tokenCountResult >= maxTokens -> truncateText(result, maxTokens)
            else -> result
        }
    }
}
