package xef

import arrow.atomic.AtomicInt
import com.xebia.functional.openai.generated.model.ChatCompletionRequestAssistantMessage
import com.xebia.functional.openai.generated.model.ChatCompletionRequestMessage
import com.xebia.functional.openai.generated.model.ChatCompletionRequestUserMessage
import com.xebia.functional.openai.generated.model.ChatCompletionRequestUserMessageContent
import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.MemorizedMessage
import com.xebia.functional.xef.store.Memory

class MemoryData {
    val defaultConversationId = ConversationId("default-id")

    val atomicInt = AtomicInt(0)

    fun generateRandomMessages(
        n: Int,
        append: String? = null,
        conversationId: ConversationId = defaultConversationId
    ): List<Memory> =
        (0 until n).flatMap {
            val m1 = ChatCompletionRequestMessage.Fifth(
                ChatCompletionRequestUserMessage(
                    role = ChatCompletionRequestUserMessage.Role.user,
                    content = ChatCompletionRequestUserMessageContent.First("Question $it${append?.let { ": $it" } ?: ""}")
                )
            )
            val m2 = ChatCompletionRequestMessage.First(
                ChatCompletionRequestAssistantMessage(
                    role = ChatCompletionRequestAssistantMessage.Role.assistant,
                    content = "Answer $it${append?.let { ": $it" } ?: ""}"
                )
            )
            listOf(
                Memory(conversationId, MemorizedMessage.Request(m1), atomicInt.addAndGet(1)),
                Memory(conversationId, MemorizedMessage.Request(m2), atomicInt.addAndGet(1)),
            )
        }
}
