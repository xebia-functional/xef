package wikipedia

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.reasoning.tools.Tool
import kotlin.jvm.JvmSynthetic

interface TestSearchWikipediaTool : Tool {

  val model: Chat
  val scope: Conversation
  val maxResultsInContext: Int
  val client: TestWikipediaClient

  override val name: String
    get() = "SearchWikipediaTool"

  override val description: String
    get() =
      "Search primary tool in Wikipedia for information. The tool input is a simple one line string"

  @JvmSynthetic
  override suspend fun invoke(input: String): String {

    val docs = client.search(TestWikipediaClient.TestSearchData(input))
    return model
      .promptMessages(
        prompt =
          Prompt {
            +system("Search results:")
            docs.searchResults.searches.take(maxResultsInContext).forEach {
              +system("Title: ${it.title}")
              +system("PageId: ${it.pageId}")
              +system("Size: ${it.size}")
              +system("WordCount: ${it.wordCount}")
              +system("Content: ${it.document}")
            }
          },
        scope = scope,
      )
      .firstOrNull()
      ?: "No results found"
  }

  fun close() {
    client.close()
  }
}
