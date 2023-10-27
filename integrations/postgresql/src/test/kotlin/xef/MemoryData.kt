package xef

import arrow.atomic.AtomicInt
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role
import com.xebia.functional.xef.store.ConversationId
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
            val m1 = Message(Role.USER, "Question $it${append?.let { ": $it" } ?: ""}", Role.USER.toString().lowercase())
            val m2 = Message(Role.ASSISTANT, "Response $it${append?.let { ": $it" } ?: ""}", Role.ASSISTANT.toString().lowercase())
            listOf(
                Memory(conversationId, m1, atomicInt.addAndGet(1)),
                Memory(conversationId, m2, atomicInt.addAndGet(1)),
            )
        }
}
