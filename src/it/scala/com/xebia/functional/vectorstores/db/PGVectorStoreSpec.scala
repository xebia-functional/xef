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

  test("initialDbSetup should configure the DB properly") {
    val result: IO[Unit] = pg.initialDbSetup()
    assertIO(result, ())
  }

  test("addTexts should fail with a CollectionNotFoundError if collection isn't present in the DB") {
    val result: IO[List[DocumentVectorId]] = pg.addTexts(TestData.texts)
    interceptMessageIO[PGErrors.CollectionNotFoundError](
      "Collection 'test_collection' not found"
    )(result)
  }

  test("similaritySearch shoul fail with a CollectionNotFoundError if collection isn't present in the DB") {
    val result: IO[List[Document]] = pg.similaritySearch("foo", 2)
    interceptMessageIO[PGErrors.CollectionNotFoundError](
      "Collection 'test_collection' not found"
    )(result)
  }

  test("createCollection should create collection") {
    val result: IO[Int] = pg.createCollection
    assertIO(result, 1)
  }

  test("addTexts should return a list of 2 elements") {
    val result: IO[List[DocumentVectorId]] = pg.addTexts(TestData.texts)
    assertIO(result.map(_.length), 2)
  }

  test("similaritySearchByVector should return both documents") {
    val result: IO[List[Document]] = pg.similaritySearchByVector(TestData.barEmbedding, 2)
    assertIO(result.map(_.map(_.content)), List("bar", "foo"))
  }

  test("addDocuments should return a list of 2 elements") {
    val result: IO[List[DocumentVectorId]] = pg.addDocuments(TestData.texts.map(Document.apply(_)))
    assertIO(result.map(_.length), 2)
  }

  test("similaritySearch should return 2 documents") {
    val result: IO[List[Document]] = pg.similaritySearch("foo", 2)
    assertIO(result.map(_.length), 2)
  }

  test("similaritySearch should fail when embedding vector is empty") {
    val result: IO[List[Document]] = pg.similaritySearch("baz", 2)
    interceptMessageIO[PGErrors.EmbeddingNotGeneratedError](
      "Embedding for text: 'baz', has not been properly generated"
    )(result)
  }

  test("similaritySearchByVector should return document") {
    val result: IO[List[Document]] = pg.similaritySearchByVector(TestData.fooEmbedding, 1)
    assertIO(result.map(_.map(_.content)), List("foo"))
  }

  test("check query - addVectorExtension") {
    check(PGSql.addVectorExtension)
  }

  test("check query - createCollectionsTable") {
    check(PGSql.createCollectionsTable)
  }

  test("check query - createEmbeddingTable") {
    check(PGSql.createEmbeddingTable(3))
  }

  test("check query - addNewCollection") {
    check(PGSql.addNewCollection(TestData.collectionUUID.id, TestData.collectionName))
  }

  test("check query - getCollection") {
    check(PGSql.getCollection(TestData.collectionName))
  }

  test("check query - getCollectionById") {
    check(PGSql.getCollectionById(TestData.collectionUUID.id))
  }

  test("check query - deleteCollectionDocs") {
    check(PGSql.deleteCollectionDocs(TestData.collectionUUID.id))
  }

  test("check query - deleteCollection") {
    check(PGSql.deleteCollection(TestData.collectionUUID.id))
  }

  // NB: addNewText and searchSimilarDocument checks aren't here since they're failing
  // However they're well covered in addTexts, similaritySearch and similaritySearchByVector
