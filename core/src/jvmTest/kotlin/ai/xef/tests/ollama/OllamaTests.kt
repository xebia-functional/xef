package ai.xef.tests.ollama

import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.prompt.ToolCallStrategy
import io.kotest.common.runBlocking
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.testcontainers.ollama.OllamaContainer
import org.testcontainers.utility.DockerImageName

@Disabled
class OllamaTests {

  companion object {
    private const val OLLAMA_IMAGE = "ollama/ollama:0.1.26"
    private const val NEW_IMAGE_NAME = "ollama/ollama:test"

    val ollama: OllamaContainer by lazy { OllamaContainer(DockerImageName.parse(OLLAMA_IMAGE)) }

    @BeforeAll
    @JvmStatic
    fun setup() {
      ollama.start()
      ollama.execInContainer("ollama", "pull", "llama3:8b")
      ollama.execInContainer("ollama", "run", "llama3:8b")
      ollama.commitToImage(NEW_IMAGE_NAME)
    }

    @AfterAll
    @JvmStatic
    fun teardown() {
      ollama.stop()
    }
  }

  suspend inline fun <reified A> llama3_8b(
    prompt: String,
    config: Config = Config(baseUrl = "http://localhost:11434/v1/"),
    api: Chat = OpenAI(config = config, logRequests = true).chat,
  ): A =
    AI(
      prompt = prompt,
      config = config,
      api = api,
      model = CreateChatCompletionRequestModel.Custom("gemma:2b"),
      toolCallStrategy = ToolCallStrategy.InferJsonFromStringResponse
    )

  @Serializable data class SolarSystemPlanet(val planet: String)

  @Test
  fun `test AI chat function`() = runBlocking {
    val result = llama3_8b<SolarSystemPlanet>("Your favorite planet")
    println(result)
  }

  // Add more tests for other functions in the AI companion object
}
