package com.xebia.functional.scala.vectorstores.postgres

import java.util.UUID

import cats.syntax.all.*

import com.xebia.functional.scala.domain
import com.xebia.functional.scala.embeddings.models.Embedding
import doobie.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.fragment.Fragment
import doobie.util.query.Query
import doobie.util.query.Query0
import doobie.util.update.Update0

object PGSql:
  def addVectorExtension: Update0 =
    sql"""
      CREATE EXTENSION IF NOT EXISTS vector
    """.update

  def createCollectionsTable: Update0 =
    sql"""
      CREATE TABLE IF NOT EXISTS langchain4s_collections (
        uuid UUID PRIMARY KEY,
        name VARCHAR UNIQUE NOT NULL
      )
    """.update

  def createEmbeddingTable(vectorSize: Int): Update0 =
    (fr"CREATE TABLE IF NOT EXISTS langchain4s_embeddings (uuid UUID PRIMARY KEY, collection_id UUID references langchain4s_collections(uuid), embedding vector(" ++ Fragment
      .const(
        vectorSize.show
      ) ++ fr"), content VARCHAR)").update

  def addNewCollection(uuid: UUID, collectionName: String): Update0 =
    sql"""
      INSERT INTO langchain4s_collections(uuid, name)
      VALUES ($uuid, $collectionName)
      ON CONFLICT DO NOTHING
    """.update

  def deleteCollection(collectionId: UUID): Update0 =
    sql"""
      DELETE FROM langchain4s_collections
      WHERE uuid = $collectionId
    """.update

  def getCollection(collectionName: String): Query0[PGCollection] =
    sql"""
      SELECT * FROM langchain4s_collections
      WHERE name = $collectionName
    """.query[PGCollection]

  def getCollectionById(uuid: UUID): Query0[PGCollection] =
    sql"""
      SELECT * FROM langchain4s_collections
      WHERE uuid = $uuid
    """.query[PGCollection]

  def addNewDocument(uuid: UUID, collectionId: UUID, document: domain.Document, embedding: Embedding): Update0 =
    sql"""
      INSERT INTO langchain4s_embeddings(uuid, collection_id, embedding, content)
      VALUES ($uuid, $collectionId, ${embedding.data}, ${document.content})
    """.update

  def deleteCollectionDocs(collectionId: UUID): Update0 =
    sql"""
      DELETE FROM langchain4s_embeddings
      WHERE collection_id = $collectionId
    """.update

  def addNewText(uuid: UUID, collectionId: UUID, text: String, embedding: Embedding): Update0 =
    sql"""
      INSERT INTO langchain4s_embeddings(uuid, collection_id, embedding, content)
      VALUES ($uuid, $collectionId, ${embedding.data}::vector, $text)
    """.update

  def searchSimilarDocument(e: Embedding, strategy: PGDistanceStrategy, collection: PGCollection, k: Int): Query0[domain.Document] =
    (fr"SELECT content FROM langchain4s_embeddings WHERE collection_id = ${collection.uuid} ORDER BY embedding " ++ Fragment
      .const(strategy.strategy) ++ fr" ${e.data.map(_.toFloat)}::vector limit $k")
      .query[domain.Document]
