package com.xebia.functional.scala.synopsisreviewssc

import scala.concurrent.duration._

import cats.data.NonEmptySeq
import cats.effect.IO
import cats.effect.IOApp
import cats.implicits.*

import com.xebia.functional.scala.chains.*
import com.xebia.functional.scala.config.*
import com.xebia.functional.scala.embeddings.openai.models.*
import com.xebia.functional.scala.llm.LLM
import com.xebia.functional.scala.llm.openai.OpenAIClient
import com.xebia.functional.scala.prompt.PromptTemplate
import com.xebia.functional.scala.vectorstores.postgres.*
import eu.timepit.refined.types.string.NonEmptyString

object SynopsisReviewSSC extends IOApp.Simple:
  override def run: IO[Unit] =
    val OPENAI_TOKEN = ""
    val openAIConfig = OpenAIConfig(OPENAI_TOKEN, 5.seconds, 5, 1000, OpenAIConfigLLM(temperature = Some(0.8), maxTokens = Some(500)))
    lazy val openAIClient = OpenAIClient[IO](openAIConfig)

    val sinopsisTempl = """
    You are a playwright. Given a title of play, it is your job to write a sinopsis for that title.

    Title: {title}
    Playwright: This is a synopsis for the above play:
    """.replace("\n", " ")

    val reviewTempl = """
    You are a play critic from the New York Times. Given the sinopsis of play, it is you job to write a review for that play.

    Play Sinopsis: {synopsis}
    Review from a New York Times play critic of the above play:
    """.replace("\n", " ")

    for
      promptTemplateS <- PromptTemplate.fromTemplate[IO](template = sinopsisTempl, inputVariables = List("title"))
      promptTemplateR <- PromptTemplate.fromTemplate[IO](template = reviewTempl, inputVariables = List("synopsis"))

      ouptuVariableS <- NonEmptyString.from("synopsis").toOption.liftTo[IO](new RuntimeException("synopsis variable is empty"))

      synopsisChain = LLMChain.make(
        llm = LLM.openAI[IO](openAIClient),
        promptTemplate = promptTemplateS,
        outputVariable = ouptuVariableS,
        onlyOutput = true
      )

      ouptuVariableR <- NonEmptyString.from("review").toOption.liftTo[IO](new RuntimeException("review variable is empty"))

      reviewChain = LLMChain.make(
        llm = LLM.openAI[IO](openAIClient),
        promptTemplate = promptTemplateR,
        outputVariable = ouptuVariableR,
        onlyOutput = true
      )

      chains = NonEmptySeq(synopsisChain, Seq(reviewChain))

      inputKey <- NonEmptyString.from("input").toOption.liftTo[IO](new RuntimeException("input variable is empty"))
      outputKey <- NonEmptyString.from("output").toOption.liftTo[IO](new RuntimeException("output variable is empty"))

      ssc <- SimpleSequentialChain.make[IO](chains, inputKey, outputKey)
      response <- ssc.run("Terror at White Mountain Peak")
      _ = println(s"input: ${response(inputKey.toString)}")
      _ = println(s"output: ${response(outputKey.toString)}")
    yield ()
