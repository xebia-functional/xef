package xef

import ai.xef.openai.StandardModel
import com.xebia.functional.openai.models.*
import com.xebia.functional.xef.store.PGVectorStore
import com.xebia.functional.xef.store.postgresql.PGDistanceStrategy
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.spec.style.scopes.StringSpecScope
import io.kotest.extensions.testcontainers.ContainerExtension
import io.kotest.matchers.shouldBe
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

    fun StringSpecScope.pg() =
      PGVectorStore(
        vectorSize = 3,
        dataSource = dataSource,
        embeddings = TestEmbeddings(coroutineContext),
        collectionName = "test_collection",
        distanceStrategy = PGDistanceStrategy.Euclidean,
        preDeleteCollection = false,
        chunkSize = null
      )

    "initialDbSetup should configure the DB properly" { pg().initialDbSetup() }

    "addTexts should fail with a CollectionNotFoundError if collection isn't present in the DB" {
      assertThrows<IllegalStateException> { pg().addTexts(listOf("foo", "bar")) }.message shouldBe
        "Collection 'test_collection' not found"
    }

    "similaritySearch should fail with a CollectionNotFoundError if collection isn't present in the DB" {
      assertThrows<IllegalStateException> { pg().similaritySearch("foo", 2) }.message shouldBe
        "Collection 'test_collection' not found"
    }

    "createCollection should create collection" { pg().createCollection() }

    "addTexts should not fail now that we created the collection" {
      pg().addTexts(listOf("foo", "bar"))
    }

    "similaritySearchByVector should return both documents" {
      pg().similaritySearchByVector(Embedding(0, listOf(4.0, 5.0, 6.0), Embedding.Object.embedding), 2) shouldBe
        listOf("bar", "foo")
    }

    "similaritySearch should return 2 documents" { pg().similaritySearch("foo", 2).size shouldBe 2 }

    "similaritySearch should fail when embedding vector is empty" {
      assertThrows<IllegalStateException> { pg().similaritySearch("baz", 2) }.message shouldBe
        "Embedding for text: 'baz', has not been properly generated"
    }

    "similaritySearchByVector should return document" {
      pg().similaritySearchByVector(
        Embedding(0, listOf(1.0, 2.0, 3.0), Embedding.Object.embedding),
        1
      ) shouldBe listOf("foo")
    }

    "the added memories sorted by index should be obtained in the same order" {
      val memoryData = MemoryData()
      val model = StandardModel(CreateFineTuneRequestModel.ada)
      val memories = memoryData.generateRandomMessages(10)
      pg().addMemories(memories)
      memories shouldBe pg().memories(model, memoryData.defaultConversationId, 1000)
    }
  })

//class TestLLM(override val modelType: ModelType = ModelType.ADA) : Chat, AutoCloseable {
//  override fun copy(modelType: ModelType) =
//    TestLLM(modelType)
//
//  override fun tokensFromMessages(messages: List<ChatCompletionRequestMessage>): Int =
//    messages.map { calculateTokens(it) }.sum()
//
//  private fun calculateTokens(message: ChatCompletionRequestMessage): Int =
//    message.contentAsString().orEmpty().split(" ").size + 2 // 2 is the role and name
//
//  override suspend fun createChatCompletion(request: CreateChatCompletionRequest): CreateChatCompletionResponse {
//    throw NotImplementedError()
//  }
//
//  override suspend fun createChatCompletions(request: CreateChatCompletionRequest): Flow<CreateChatCompletionStreamResponse> {
//    throw NotImplementedError()
//  }
//
//  override fun close() {
//    throw NotImplementedError()
//  }
//}
//
//private fun Embeddings.Companion.mock(
//  embedDocuments:
//  suspend (texts: List<String>, chunkSize: Int?) -> List<Embedding> =
//    { _, _ ->
//      listOf(
//        Embedding(0, listOf(1.0, 2.0, 3.0), Embedding.Object.embedding),
//        Embedding(1, listOf(4.0, 5.0, 6.0), Embedding.Object.embedding)
//      )
//    },
//  embedQuery: suspend (text: String) -> List<Embedding> = { text ->
//    when (text) {
//      "foo" -> listOf(Embedding(0, listOf(1.0, 2.0, 3.0), Embedding.Object.embedding))
//      "bar" -> listOf(Embedding(0, listOf(4.0, 5.0, 6.0), Embedding.Object.embedding))
//      "baz" -> listOf()
//      else -> listOf()
//    }
//  },
//  createEmbeddings: suspend (request: CreateEmbeddingRequest) -> CreateEmbeddingResponse = { _ ->
//    CreateEmbeddingResponse(
//      data = listOf(
//        Embedding(0, listOf(1.0, 2.0, 3.0), Embedding.Object.embedding),
//        Embedding(1, listOf(4.0, 5.0, 6.0), Embedding.Object.embedding)
//      ),
//      model = "test-model",
//      `object` = CreateEmbeddingResponse.Object.list,
//      usage = CreateEmbeddingResponseUsage(0, 0)
//    )
//  }
//): Embeddings =
//  object : Embeddings {
//    override fun copy(modelType: ModelType): LLM {
//      throw NotImplementedError()
//    }
//
//    override suspend fun embedDocuments(
//      texts: List<String>,
//      chunkSize: Int?
//    ): List<Embedding> = embedDocuments(texts, chunkSize)
//
//    override suspend fun embedQuery(text: String): List<Embedding> =
//      embedQuery(text)
//
//    override suspend fun createEmbeddings(request: CreateEmbeddingRequest): CreateEmbeddingResponse =
//      createEmbeddings(request)
//
//
//    override val modelType: ModelType = ModelType.TODO("test-embeddings")
//  }
