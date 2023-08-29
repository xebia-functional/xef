package wikipedia

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.Chat

class TestSearchWikipedia(
  override val model: Chat,
  override val scope: Conversation,
  override val maxResultsInContext: Int = 3,
) : TestSearchWikipediaTool, AutoCloseable {

  override val client: TestWikipediaClient = TestWikipediaClient()

  override fun close() {
    client.close()
  }
}
