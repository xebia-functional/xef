package com.xebia.functional.vectorstores.postgres

import java.util.UUID

import cats.effect.Resource
import cats.effect.Sync
import cats.syntax.all.*

import com.xebia.functional.config.DBConfig
import com.xebia.functional.domain.Document
import com.xebia.functional.embeddings.Embeddings
import com.xebia.functional.embeddings.models.Embedding
import com.xebia.functional.embeddings.openai.models.RequestConfig
import com.xebia.functional.vectorstores.VectorStore
import com.xebia.functional.vectorstores.models.DocumentVectorId
import com.xebia.functional.vectorstores.postgres.PGSql.*
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.transactor.Transactor

class PGVectorStore[F[_]: Sync](
    dbConfig: DBConfig,
    embeddings: Embeddings[F],
    collectionName: String,
    distanceStrategy: PGDistanceStrategy,
    preDeleteCollection: Boolean,
    transactor: Resource[F, Transactor[F]],
    requestConfig: RequestConfig,
    chunckSize: Option[Int]
) extends VectorStore[F]:
  def delCollection: ConnectionIO[Unit] =
    preDeleteCollection match
      case false => Sync[ConnectionIO].unit
      case true =>
        for
          collection <- getCollection(collectionName).option
          c <- collection.liftTo[ConnectionIO](PGErrors.CollectionNotFound(collectionName))
          _ <- deleteCollectionDocs(c.uuid).run
          _ <- deleteCollection(c.uuid).run
        yield ()

  def initialDbSetup(): F[Unit] =
    transactor
      .use { xa =>
        (for {
          _ <- addVectorExtension.run
          _ <- createCollectionsTable.run
          _ <- createEmbeddingTable(dbConfig.vectorSize).run
          _ <- delCollection
        } yield ()).transact(xa)
      }.adaptErr { case e: Throwable => PGErrors.DatabaseSetupError(e.getMessage) }

  def createCollection: F[Int] =
    transactor.use { xa =>
      memeid4s.UUID.V1.next
        .asJava()
        .pure.flatMap { uuid =>
          addNewCollection(uuid, collectionName).run.transact(xa)
        }
    }

  def addTexts(texts: List[String]): F[List[DocumentVectorId]] =
    transactor.use { xa =>
      for
        embs <- embeddings.embedDocuments(texts, chunckSize, requestConfig)
        pairs = texts zip embs
        us <-
          (for
            collection <- getCollection(collectionName).option
            c <- collection.liftTo[ConnectionIO](PGErrors.CollectionNotFound(collectionName))
            uuids <-
              pairs.traverse((t, e) =>
                memeid4s.UUID.V1.next
                  .asJava()
                  .pure[ConnectionIO]
                  .flatTap(u => addNewText(u, c.uuid, t, e).run)
              )
          yield (uuids.map(DocumentVectorId(_)))).transact(xa)
      yield us
    }

  def addDocuments(documents: List[Document]): F[List[DocumentVectorId]] =
    addTexts(documents.map(_.content))

  def similaritySearch(query: String, k: Int): F[List[Document]] =
    transactor.use { xa =>
      for
        qEmb <- embeddings.embedQuery(query, requestConfig)
        docs <- qEmb match
          case e +: es => searchSimilarDocument(e, distanceStrategy, k).to[List].transact(xa)
          case _ => Sync[F].raiseError(PGErrors.EmbeddingNotGenerated(query))
      yield docs
    }

  def similaritySearchByVector(embedding: Embedding, k: Int): F[List[Document]] =
    transactor.use { xa =>
      searchSimilarDocument(embedding, distanceStrategy, k).to[List].transact(xa)
    }

object PGVectorStore:
  def make[F[_]: Sync](
      config: DBConfig,
      embeddings: Embeddings[F],
      collectionName: String,
      strategy: PGDistanceStrategy,
      preDeleteCollection: Boolean,
      transactor: Resource[F, Transactor[F]],
      requestConfig: RequestConfig,
      chunkSize: Option[Int]
  ): PGVectorStore[F] =
    new PGVectorStore[F](config, embeddings, collectionName, strategy, preDeleteCollection, transactor, requestConfig, chunkSize)

  def fromTexts[F[_]: Sync](
      texts: List[String],
      requestConfig: RequestConfig,
      config: DBConfig,
      embeddings: Embeddings[F],
      collectionName: String,
      strategy: PGDistanceStrategy,
      preDeleteCollection: Boolean,
      transactor: Resource[F, Transactor[F]],
      chunkSize: Option[Int]
  ): F[PGVectorStore[F]] =
    for
      store <- Sync[F].pure(make[F](config, embeddings, collectionName, strategy, preDeleteCollection, transactor, requestConfig, chunkSize))
      _ <- store.initialDbSetup()
      _ <- store.createCollection
      _ <- store.addTexts(texts)
    yield (store)

  def fromDocuments[F[_]: Sync](
      documents: List[Document],
      requestConfig: RequestConfig,
      config: DBConfig,
      embeddings: Embeddings[F],
      collectionName: String,
      strategy: PGDistanceStrategy,
      preDeleteCollection: Boolean,
      transactor: Resource[F, Transactor[F]],
      chunkSize: Option[Int]
  ): F[PGVectorStore[F]] =
    for
      store <- Sync[F].pure(make[F](config, embeddings, collectionName, strategy, preDeleteCollection, transactor, requestConfig, chunkSize))
      _ <- store.initialDbSetup()
      _ <- store.createCollection
      _ <- store.addDocuments(documents)
    yield (store)
