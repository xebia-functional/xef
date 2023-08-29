package wikipedia

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.Chat

class TestSearchWikipediaByPageId(override val model: Chat, override val scope: Conversation) :
  TestSearchWikipediaByPageIdTool, AutoCloseable {

  override val client: TestWikipediaByTitleAndPageIdClient = TestWikipediaByTitleAndPageIdClient()

  override fun close() {
    client.close()
  }
}
