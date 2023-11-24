package xef

import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.Embeddings
import com.xebia.functional.xef.llm.LLM
import com.xebia.functional.xef.llm.models.MaxIoContextLength
import com.xebia.functional.xef.llm.models.ModelID
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.embeddings.Embedding
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig
import com.xebia.functional.xef.llm.models.usage.Usage
import com.xebia.functional.xef.store.PGVectorStore
import com.xebia.functional.xef.store.postgresql.PGDistanceStrategy
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.testcontainers.ContainerExtension
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import org.junit.jupiter.api.assertThrows
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

val postgres: PostgreSQLContainer<Nothing> =
  PostgreSQLContainer(
    DockerImageName.parse("ankane/pgvector").asCompatibleSubstituteFor("postgres")
  )

class PGVectorStoreSpec :
  StringSpec({
    val container = install(ContainerExtension(postgres))
    val dataSource =
      autoClose(
        HikariDataSource(
          HikariConfig().apply {
            jdbcUrl = container.jdbcUrl.replace("localhost", "0.0.0.0")
            username = container.username
            password = container.password
            driverClassName = "org.postgresql.Driver"
          }
        )
      )

    val pg =
      PGVectorStore(
        vectorSize = 3,
        dataSource = dataSource,
        embeddings = Embeddings.mock(),
        collectionName = "test_collection",
        distanceStrategy = PGDistanceStrategy.Euclidean,
        preDeleteCollection = false,
        requestConfig = RequestConfig(RequestConfig.Companion.User("user")),
        chunkSize = null
      )

    "initialDbSetup should configure the DB properly" { pg.initialDbSetup() }

    "addTexts should fail with a CollectionNotFoundError if collection isn't present in the DB" {
      assertThrows<IllegalStateException> { pg.addTexts(listOf("foo", "bar")) }.message shouldBe
        "Collection 'test_collection' not found"
    }

    "similaritySearch should fail with a CollectionNotFoundError if collection isn't present in the DB" {
      assertThrows<IllegalStateException> { pg.similaritySearch("foo", 2) }.message shouldBe
        "Collection 'test_collection' not found"
    }

    "createCollection should create collection" { pg.createCollection() }

    "addTexts should not fail now that we created the collection" {
      pg.addTexts(listOf("foo", "bar"))
    }

    "similaritySearchByVector should return both documents" {
      pg.similaritySearchByVector(Embedding(listOf(4.0f, 5.0f, 6.0f)), 2) shouldBe
        listOf("bar", "foo")
    }

    "similaritySearch should return 2 documents" { pg.similaritySearch("foo", 2).size shouldBe 2 }

    "similaritySearch should fail when embedding vector is empty" {
      assertThrows<IllegalStateException> { pg.similaritySearch("baz", 2) }.message shouldBe
        "Embedding for text: 'baz', has not been properly generated"
    }

    "similaritySearchByVector should return document" {
      pg.similaritySearchByVector(Embedding(listOf(1.0f, 2.0f, 3.0f)), 1) shouldBe listOf("foo")
    }

    "the added memories sorted by index should be obtained in the same order" {
      val memoryData = MemoryData()
      val llm = TestLLM()
      val memories = memoryData.generateRandomMessages(10)
      pg.addMemories(memories)
      memories shouldBe pg.memories(llm, memoryData.defaultConversationId, 1000)
    }
  })

class TestLLM : Chat, AutoCloseable {

  override val modelID = ModelID("test-llm")
  override val contextLength = MaxIoContextLength.Combined(Integer.MAX_VALUE)

  override fun copy(modelID: ModelID) =
    TestLLM()

  override fun tokensFromMessages(messages: List<Message>): Int = messages.map { calculateTokens(it) }.sum()

  override fun countTokens(text: String): Int = text.length

  override fun truncateText(text: String, maxTokens: Int): String = text

  private fun calculateTokens(message: Message): Int = message.content.split(" ").size + 2 // 2 is the role and name

  override suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse {
    throw NotImplementedError()
  }

  override suspend fun createChatCompletions(request: ChatCompletionRequest): Flow<ChatCompletionChunk> {
    throw NotImplementedError()
  }

  override fun close() {
    throw NotImplementedError()
  }
}

private fun Embeddings.Companion.mock(
  embedDocuments:
  suspend (texts: List<String>, config: RequestConfig, chunkSize: Int?) -> List<Embedding> =
    { _, _, _ ->
      listOf(Embedding(listOf(1.0f, 2.0f, 3.0f)), Embedding(listOf(4.0f, 5.0f, 6.0f)))
    },
  embedQuery: suspend (text: String, config: RequestConfig) -> List<Embedding> = { text, _ ->
    when (text) {
      "foo" -> listOf(Embedding(listOf(1.0f, 2.0f, 3.0f)))
      "bar" -> listOf(Embedding(listOf(4.0f, 5.0f, 6.0f)))
      "baz" -> listOf()
      else -> listOf()
    }
  },
  createEmbeddings: suspend (request: EmbeddingRequest) -> EmbeddingResult = { _ ->
    EmbeddingResult(listOf(Embedding(listOf(1.0f, 2.0f, 3.0f)), Embedding(listOf(4.0f, 5.0f, 6.0f))), Usage.ZERO)
  }
): Embeddings =
  object : Embeddings {
    override fun copy(modelID: ModelID): LLM {
      throw NotImplementedError()
    }
    override suspend fun embedDocuments(
      texts: List<String>,
      requestConfig: RequestConfig,
      chunkSize: Int?
    ): List<Embedding> = embedDocuments(texts, requestConfig, chunkSize)

    override suspend fun embedQuery(text: String, requestConfig: RequestConfig): List<Embedding> =
      embedQuery(text, requestConfig)

    override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult =
      createEmbeddings(request)


    override val modelID = ModelID("test-embeddings")
  }
