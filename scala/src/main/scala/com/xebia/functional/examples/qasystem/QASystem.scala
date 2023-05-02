package com.xebia.functional.qasystem

import java.io.File

import scala.concurrent.duration._

import cats.effect.IO
import cats.effect.IOApp

import com.xebia.functional.chains.retrievalqa.QAPrompt
import com.xebia.functional.chains.retrievalqa.VectorQAChain
import com.xebia.functional.config.DBConfig
import com.xebia.functional.config.OpenAIConfig
import com.xebia.functional.config.OpenAIConfigLLM
import com.xebia.functional.domain.Document
import com.xebia.functional.embeddings.openai.OpenAIEmbeddings
import com.xebia.functional.embeddings.openai.models.EmbeddingModel
import com.xebia.functional.embeddings.openai.models.RequestConfig
import com.xebia.functional.embeddings.openai.models.RequestConfig.User
import com.xebia.functional.llm.*
import com.xebia.functional.llm.models.OpenAIRequest
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.loaders.TextLoader
import com.xebia.functional.vectorstores.db.DoobieTransactor
import com.xebia.functional.vectorstores.postgres.PGDistanceStrategy
import com.xebia.functional.vectorstores.postgres.PGVectorStore
import eu.timepit.refined.types.string.NonEmptyString
import org.typelevel.log4cats.slf4j.Slf4jLogger

object QASystem extends IOApp.Simple {

  override def run: IO[Unit] =
    val OPENAI_TOKEN = "<place-your-openai-token-here>"

    val openAIConfig = OpenAIConfig(OPENAI_TOKEN, 5.seconds, 5, 1000, OpenAIConfigLLM())
    val requestConfig = RequestConfig(EmbeddingModel.TextEmbeddingAda002, User("testing"))
    val dbConfig = DBConfig(
      "jdbc:postgresql://localhost:5432/postgres",
      "postgres",
      "password",
      "org.postgresql.Driver",
      2,
      1536
    )

    val collectionName: String = "expenditures"

    for
      path <- getResourcePath("/expenditures_2020.txt")
      txLr = new TextLoader[IO](path)
      docs <- txLr.load
      lggr <- Slf4jLogger.create[IO]
      openAIClient = OpenAIClient[IO](openAIConfig)
      openAIEmbedd = new OpenAIEmbeddings[IO](openAIConfig, openAIClient, lggr)
      pg <- PGVectorStore.fromDocuments[IO](
        docs,
        requestConfig,
        dbConfig,
        openAIEmbedd,
        collectionName,
        PGDistanceStrategy.Euclidean,
        false,
        DoobieTransactor.make[IO](dbConfig),
        None
      )

      outputVariable = NonEmptyString.unsafeFrom("answer")
      qa = VectorQAChain.make[IO](LLM.openAI[IO](openAIClient), pg, "stuff", 10, outputVariable, true)
      response <- qa.run("How could I have saved money in december 2022?")

      _ = println(response)
    yield ()

  def getResourcePath(path: String): IO[String] =
    getFile(path).map(_.toURI.getPath)

  def getFile(path: String): IO[File] =
    IO {
      val classLoader = getClass.getClassLoader
      new File(getClass.getResource(path).getFile)
    }

}
