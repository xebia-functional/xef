package com.xebia.functional.vectorstores.db

import java.util.UUID

import com.xebia.functional.config.DBConfig
import com.xebia.functional.embeddings.models.Embedding
import com.xebia.functional.embeddings.openai.models.EmbeddingModel
import com.xebia.functional.embeddings.openai.models.RequestConfig
import com.xebia.functional.vectorstores.postgres.PGDistanceStrategy
import com.xebia.functional.vectorstores.models.DocumentVectorId

object TestData:
  val docUUID: DocumentVectorId = DocumentVectorId(UUID.fromString("c38160a5-845f-4cce-a36e-f40d941dbe2e"))
  val collectionUUID: DocumentVectorId = DocumentVectorId(UUID.fromString("33d4f672-1e19-48b7-9a06-e8b52b492452"))
  val texts: List[String] = List("foo", "bar")
  val dbConf: DBConfig = DBConfig("", "", "", "", 2, 3)
  val collectionName: String = "test_collection"
  val defaultStrategy: PGDistanceStrategy = PGDistanceStrategy.Euclidean
  val testRequestConfig: RequestConfig = RequestConfig(EmbeddingModel.TextEmbeddingAda002, RequestConfig.User("user"))
  val fooEmbedding: Embedding = Embedding(Vector(1.0, 2.0, 3.0))
  val barEmbedding: Embedding = Embedding(Vector(4.0, 5.0, 6.0))
  val docEmbedding: Vector[Embedding] = Vector(Embedding(Vector(1.0, 2.0, 3.0)), Embedding(Vector(4.0, 5.0, 6.0)))
