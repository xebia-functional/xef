package com.xebia.functional.searchsimilardocs

import java.io.File

import scala.concurrent.duration._

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import cats.syntax.all.*

import com.xebia.functional.config.Config
import com.xebia.functional.config.DBConfig
import com.xebia.functional.config.OpenAIConfig
import com.xebia.functional.embeddings.openai.OpenAIEmbeddings
import com.xebia.functional.embeddings.openai.models.EmbeddingModel
import com.xebia.functional.embeddings.openai.models.RequestConfig
import com.xebia.functional.embeddings.openai.models.RequestConfig.User
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.loaders.LoaderError
import com.xebia.functional.loaders.TextLoader
import com.xebia.functional.vectorstores.db.DoobieTransactor
import com.xebia.functional.vectorstores.postgres.PGDistanceStrategy
import com.xebia.functional.vectorstores.postgres.PGVectorStore
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.slf4j.Slf4jLogger

object SearchSimilarDocs extends IOApp.Simple {

  def run: IO[Unit] =
    val OPENAI_TOKEN = "<place-your-openai-token-here>"

    val openAIConfig = OpenAIConfig(OPENAI_TOKEN, 5.seconds, 5, 1000)
    val requestConfig = RequestConfig(EmbeddingModel.TextEmbeddingAda002, User("testing"))
    val dbConfig = DBConfig(
      "jdbc:postgresql://localhost:5432/postgres",
      "postgres",
      "password",
      "org.postgresql.Driver",
      2,
      1536
    )

    val collectionName: String = "llms"
    val query: String = "openai"

    for
      path <- getResourcePath("/llms.txt")
      txLr = new TextLoader[IO](path)
      docs <- txLr.load
      lggr <- Slf4jLogger.create[IO]
      openAIClient = OpenAIClient[IO](openAIConfig)
      openAIEmbedd = new OpenAIEmbeddings[IO](openAIConfig, openAIClient, lggr)
      pg = PGVectorStore.make[IO](
        dbConfig,
        openAIEmbedd,
        collectionName,
        PGDistanceStrategy.Euclidean,
        false,
        DoobieTransactor.make[IO](dbConfig),
        requestConfig,
        None
      )
      _ <- pg.initialDbSetup()
      _ <- pg.createCollection
      _ <- pg.addDocuments(docs)
      v <- pg.similaritySearch(query, 3)
      _ = println(v)
    yield ()

  def getResourcePath(path: String): IO[String] =
    getFile(path).map(_.toURI.getPath)

  def getFile(path: String): IO[File] =
    IO {
      val classLoader = getClass.getClassLoader
      new File(getClass.getResource(path).getFile)
    }

}
