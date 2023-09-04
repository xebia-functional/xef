package xef

import com.xebia.functional.xef.llm.Embeddings
import com.xebia.functional.xef.llm.models.embeddings.Embedding
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig
import com.xebia.functional.xef.store.ConversationId
import com.xebia.functional.xef.store.Memory
import com.xebia.functional.xef.store.PGVectorStore
import com.xebia.functional.xef.store.postgresql.PGDistanceStrategy
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.testcontainers.ContainerExtension
import io.kotest.matchers.shouldBe
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
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

    "memories added in chronological order should be obtained in the same order" {
      val messages = 10
      val conversationId = ConversationId(UUID.generateUUID().toString())
      val memories = (0 until messages).flatMap {
        val m1 = Message(Role.USER, "question $it", "user")
        val m2 = Message(Role.ASSISTANT, "answer $it", "assistant")
        listOf(
          Memory(conversationId, m1, 2 * it.toLong(), calculateTokens(m1)),
          Memory(conversationId, m2, 2 * it.toLong() + 1, calculateTokens(m2))
        )
      }
      pg.addMemories(memories)
      memories shouldBe pg.memories(conversationId, memories.size)
    }
  })

private fun calculateTokens(message: Message): Int = message.content.split(" ").size + 2 // 2 is the role and name

private fun Embeddings.Companion.mock(
  embedDocuments:
  suspend (texts: List<String>, chunkSize: Int?, config: RequestConfig) -> List<Embedding> =
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
  }
): Embeddings =
  object : Embeddings {
    override suspend fun embedDocuments(
      texts: List<String>,
      requestConfig: RequestConfig,
      chunkSize: Int?
    ): List<Embedding> = embedDocuments(texts, requestConfig, chunkSize)

    override suspend fun embedQuery(text: String, requestConfig: RequestConfig): List<Embedding> =
      embedQuery(text, requestConfig)

    override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult =
      createEmbeddings(request)


    override val name: String
      get() = "embeddings"
  }
