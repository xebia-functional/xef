package com.xebia.functional.xef.ollama.tests

import arrow.fx.coroutines.parMap
import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.ollama.OllamaContainer
import org.testcontainers.utility.DockerImageName

abstract class OllamaTests {

  val logger = KotlinLogging.logger {}

  companion object {
    private const val OLLAMA_IMAGE = "ollama/ollama:0.1.26"
    private const val NEW_IMAGE_NAME = "ollama/ollama:test"

    val ollama: OllamaContainer by lazy {
      // check if the new image is already present otherwise pull the image
      if (DockerImageName.parse(NEW_IMAGE_NAME).asCompatibleSubstituteFor(OLLAMA_IMAGE) == null) {
        OllamaContainer(DockerImageName.parse(OLLAMA_IMAGE))
      } else {
        OllamaContainer(DockerImageName.parse(NEW_IMAGE_NAME))
      }
    }

    @BeforeAll
    @JvmStatic
    fun setup() {
      ollama.start()
      ollama.commitToImage(NEW_IMAGE_NAME)
    }

    @AfterAll
    @JvmStatic
    fun teardown() {
      ollama.commitToImage(NEW_IMAGE_NAME)
      ollama.stop()
    }
  }

  suspend inline fun <reified A> ollama(
    models: Set<String>,
    prompt: String,
    config: Config = Config(baseUrl = ollamaBaseUrl(), supportsLogitBias = false),
    api: Chat = OpenAI(config = config, logRequests = true).chat,
  ): List<A> {
    // pull all models
    models.parMap(context = Dispatchers.IO) { model ->
      logger.info { "🚢 Pulling model $model" }
      val pullResult = ollama.execInContainer("ollama", "pull", model)
      if (pullResult.exitCode != 0) {
        logger.error { pullResult.stderr }
        throw RuntimeException("Failed to pull model $model")
      }
      logger.info { pullResult.stdout }
      logger.info { "🚢 Pulled $model" }
    }
    // run all models
    models.parMap(context = Dispatchers.IO) { model ->
      logger.info { "🚀 Starting model $model" }
      val runResult = ollama.execInContainer("ollama", "run", model)
      if (runResult.exitCode != 0) {
        logger.error { runResult.stderr }
        throw RuntimeException("Failed to run model $model")
      }
      logger.info { runResult.stdout }
      println("🚀 Started $model")
    }
    // run inference on all models
    return models.parMap(context = Dispatchers.IO) { model ->
      logger.info { "🚀 Running inference on model $model" }
      val result: A =
        AI(
          prompt = prompt,
          config = config,
          api = api,
          model = CreateChatCompletionRequestModel.Custom(model),
        )
      logger.info { "🚀 Inference on model $model: $result" }
      result
    }
  }

  fun ollamaBaseUrl(): String =
    "http://${ollama.host}:${ollama.getMappedPort(ollama.exposedPorts.first())}/v1/"
}
