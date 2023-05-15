package com.xebia.functional.xef

import com.xebia.functional.xef.embeddings.Embedding
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.embeddings.mock
import com.xebia.functional.xef.llm.openai.EmbeddingModel
import com.xebia.functional.xef.llm.openai.RequestConfig
import com.xebia.functional.xef.vectorstores.PGDistanceStrategy
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.testcontainers.SharedTestContainerExtension
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
    val container = install(SharedTestContainerExtension(postgres))
    val dataSource =
      autoClose(
        HikariDataSource(
          HikariConfig().apply {
            jdbcUrl = container.jdbcUrl
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
        requestConfig =
          RequestConfig(EmbeddingModel.TextEmbeddingAda002, RequestConfig.Companion.User("user")),
        chunckSize = null
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
  })
