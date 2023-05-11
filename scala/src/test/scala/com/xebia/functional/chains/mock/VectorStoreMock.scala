package com.xebia.functional.scala.chains.mock

import java.util.UUID

import cats.effect.IO

import com.xebia.functional.scala.chains.TestData
import com.xebia.functional.scala.domain.Document
import com.xebia.functional.scala.embeddings.models.Embedding
import com.xebia.functional.scala.vectorstores.VectorStore
import com.xebia.functional.scala.vectorstores.models.DocumentVectorId

class VectorStoreMock extends VectorStore[IO]:

  override def addTexts(texts: List[String]): IO[List[DocumentVectorId]] =
    val docs = texts.foldLeft(Map.empty[UUID, Document]) { (acc, text) =>
      acc ++ Map(memeid4s.UUID.V1.next.asJava() -> Document(text))
    }

    TestData.ref
      .flatMap(_.update(_ ++ docs))
      .map(Unit => docs.keys.toList.map(DocumentVectorId.apply))

  override def addDocuments(documents: List[Document]): IO[List[DocumentVectorId]] =
    val docs = documents.foldLeft(Map.empty[UUID, Document]) { (acc, doc) =>
      acc ++ Map(memeid4s.UUID.V1.next.asJava() -> doc)
    }

    TestData.ref
      .flatMap(_.update(_ ++ docs))
      .map(Unit => docs.keys.toList.map(DocumentVectorId.apply))

  override def similaritySearch(query: String, k: Int): IO[List[Document]] =
    TestData.ref.flatMap(_.get).map(_.values.toList.take(k))

  override def similaritySearchByVector(embedding: Embedding, k: Int): IO[List[Document]] =
    TestData.ref.flatMap(_.get).map(_.values.toList.take(k))

object VectorStoreMock:
  def make: VectorStore[IO] = new VectorStoreMock
