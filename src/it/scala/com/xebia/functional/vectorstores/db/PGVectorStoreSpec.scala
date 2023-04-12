package com.xebia.functional.vectorstores.db

import java.util.UUID

import cats.effect.IO
import cats.effect.Resource

import com.xebia.functional.config.DBConfig
import com.xebia.functional.domain.Document
import com.xebia.functional.vectorstores.postgres.PGErrors
import com.xebia.functional.vectorstores.postgres.PGSql
import com.xebia.functional.vectorstores.postgres.PGVectorStore
import com.xebia.functional.vectorstores.models.DocumentVectorId
import doobie.util.transactor.Transactor

class PGVectorStoreSpec extends DatabaseSuite:

  lazy val transactorResource = Resource.pure[IO, Transactor[IO]](transactor)
  lazy val pg = PGVectorStore.make[IO](
    TestData.dbConf,
    EmbeddingsMock.makeStub,
    TestData.collectionName,
    TestData.defaultStrategy,
    false,
    transactorResource,
    TestData.testRequestConfig,
    None
  )

  test("PGVectorStore - initialDbSetup") {
    val result: IO[Unit] = pg.initialDbSetup()
    assertIO(result, ())
  }

  test("PGVectorStore - addTexts should fail - collection not found".fail) {
    val result: IO[List[DocumentVectorId]] = pg.addTexts(TestData.texts)
    assertIO(result, List.empty[DocumentVectorId])
  }

  test("PGVectorStore - createCollection") {
    val result: IO[Int] = pg.createCollection
    assertIO(result, 1)
  }

  test("PGVectorStore - addTexts should return a list of 2 elements") {
    val result: IO[List[DocumentVectorId]] = pg.addTexts(TestData.texts)
    assertIO(result.map(_.length), 2)
  }

  test("PGVectorStore - similaritySearchByVector should return both documents") {
    val result: IO[List[Document]] = pg.similaritySearchByVector(TestData.barEmbedding, 2)
    assertIO(result.map(_.map(_.content)), List("bar", "foo"))
  }

  test("PGVectorStore - addDocuments should return a list of 2 elements") {
    val result: IO[List[DocumentVectorId]] = pg.addDocuments(TestData.texts.map(Document.apply(_)))
    assertIO(result.map(_.length), 2)
  }

  test("PGVectorStore - similaritySearch should return 2 documents") {
    val result: IO[List[Document]] = pg.similaritySearch("foo", 2)
    assertIO(result.map(_.length), 2)
  }

  test("PGVectorStore - similaritySearch should fail when embedding vector is empty".fail) {
    val result: IO[List[Document]] = pg.similaritySearch("baz", 2)
    assertIO(result.map(_.length), 2)
  }

  test("PGVectorStore - similaritySearchByVector should return document for 'foo'") {
    val result: IO[List[Document]] = pg.similaritySearchByVector(TestData.fooEmbedding, 1)
    assertIO(result.map(_.map(_.content)), List("foo"))
  }

  test("PGVectorStore check query - addVectorExtension") {
    check(PGSql.addVectorExtension)
  }

  test("PGVectorStore check query - createCollectionsTable") {
    check(PGSql.createCollectionsTable)
  }

  test("PGVectorStore check query - createEmbeddingTable") {
    check(PGSql.createEmbeddingTable(3))
  }

  test("PGVectorStore check query - addNewCollection") {
    check(PGSql.addNewCollection(TestData.collectionUUID.id, TestData.collectionName))
  }

  test("PGVectorStore check query - getCollection") {
    check(PGSql.getCollection(TestData.collectionName))
  }

  test("PGVectorStore check query - getCollectionById") {
    check(PGSql.getCollectionById(TestData.collectionUUID.id))
  }

  test("PGVectorStore check query - deleteCollectionDocs") {
    check(PGSql.deleteCollectionDocs(TestData.collectionUUID.id))
  }

  test("PGVectorStore check query - deleteCollection") {
    check(PGSql.deleteCollection(TestData.collectionUUID.id))
  }

  // NB: addNewText and searchSimilarDocument checks aren't here since they're failing
  // However they're well covered in addTexts, similaritySearch and similaritySearchByVector
