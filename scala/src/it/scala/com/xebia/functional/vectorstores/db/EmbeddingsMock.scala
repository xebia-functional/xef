package com.xebia.functional.vectorstores.db

import cats.effect.IO

import com.xebia.functional.embeddings.Embeddings
import com.xebia.functional.embeddings.models.Embedding
import com.xebia.functional.embeddings.openai.models.RequestConfig

class EmbeddingsMock extends Embeddings[IO]:
  override def embedDocuments(texts: List[String], chunkSize: Option[Int], config: RequestConfig): IO[Vector[Embedding]] =
    IO(TestData.docEmbedding)

  override def embedQuery(text: String, config: RequestConfig): IO[Vector[Embedding]] = IO(text match
    case "foo" => Vector(TestData.fooEmbedding)
    case "bar" => Vector(TestData.barEmbedding)
    case "baz" => Vector.empty[Embedding]
  )

object EmbeddingsMock:
  def makeStub: EmbeddingsMock = new EmbeddingsMock
