package com.xebia.functional.xef.ollama.tests

import com.github.dockerjava.api.model.Image
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import org.junit.jupiter.api.AfterAll
import org.testcontainers.DockerClientFactory
import org.testcontainers.ollama.OllamaContainer
import org.testcontainers.utility.DockerImageName

abstract class OllamaTests {

  val logger = KotlinLogging.logger {}

  companion object {
    private const val OLLAMA_IMAGE = "ollama/ollama:0.1.26"

    private val registeredContainers: MutableMap<String, OllamaContainer> = ConcurrentHashMap()

    @PublishedApi
    internal fun useModel(model: String): OllamaContainer =
      if (registeredContainers.containsKey(model)) {
        registeredContainers[model]!!
      } else {
        ollamaContainer(model)
      }

    private fun ollamaContainer(model: String, imageName: String = model): OllamaContainer {
      if (registeredContainers.containsKey(model)) {
        return registeredContainers[model]!!
      }
      // create the new image if it is not already a docker image
      val listImagesCmd: List<Image> =
        DockerClientFactory.lazyClient().listImagesCmd().withImageNameFilter(imageName).exec()

      val ollama =
        if (listImagesCmd.isEmpty()) {
          // ship container emoji: 🚢
          println("🐳 Creating a new Ollama container with $model image...")
          val ollama = OllamaContainer(OLLAMA_IMAGE)
          ollama.start()
          println("🐳 Pulling $model image...")
          ollama.execInContainer("ollama", "pull", model)
          println("🐳 Committing $model image...")
          ollama.commitToImage(imageName)
          ollama.withReuse(true)
        } else {
          println("🐳 Using existing Ollama container with $model image...")
          // Substitute the default Ollama image with our model variant
          val ollama =
            OllamaContainer(
                DockerImageName.parse(imageName).asCompatibleSubstituteFor("ollama/ollama")
              )
              .withReuse(true)
          ollama.start()
          ollama
        }
      println("🐳 Starting Ollama container with $model image...")
      registeredContainers[model] = ollama
      ollama.execInContainer("ollama", "run", model)
      return ollama
    }

    @AfterAll
    @JvmStatic
    fun teardown() {
      registeredContainers.forEach { (model, container) ->
        println("🐳 Stopping Ollama container for model $model")
        container.stop()
      }
    }
  }

  protected suspend inline fun <reified A> ollama(
    model: String,
    prompt: String,
  ): A {
    useModel(model)
    val config = Config(supportsLogitBias = false, baseUrl = ollamaBaseUrl(model))
    val api = OpenAI(config = config, logRequests = true).chat
    val result: A =
      AI(
        prompt = prompt,
        config = config.copy(),
        api = api,
        model = CreateChatCompletionRequestModel.Custom(model),
      )
    logger.info { "🚀 Inference on model $model: $result" }
    return result
  }

  fun ollamaBaseUrl(model: String): String {
    val ollama = registeredContainers[model]!!
    return "http://${ollama.host}:${ollama.getMappedPort(ollama.exposedPorts.first())}/v1/"
  }
}
