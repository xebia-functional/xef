package serpapi

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.Chat

class TestSearch(
  override val model: Chat,
  override val scope: Conversation,
  override val maxResultsInContext: Int = 3,
) : TestSearchTool, AutoCloseable {

  override val client = TestSerpApiClient()

  override fun close() {
    client.close()
  }
}
