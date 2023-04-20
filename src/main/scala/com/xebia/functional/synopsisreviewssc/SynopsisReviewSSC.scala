package com.xebia.functional.synopsisreviewssc

import cats.effect.{IO, IOApp}
import com.xebia.functional.config.*

import scala.concurrent.duration._
import com.xebia.functional.chains.*
import com.xebia.functional.embeddings.openai.models.*
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.vectorstores.postgres.*
import eu.timepit.refined.types.string.NonEmptyString
import cats.data.NonEmptySeq
import com.xebia.functional.prompt.PromptTemplate

object SynopsisReviewSSC extends IOApp.Simple:
  override def run: IO[Unit] =
    val OPENAI_TOKEN = ""
    val openAIConfig = OpenAIConfig(OPENAI_TOKEN, 5.seconds, 5, 1000)
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

      ouptuVariableS = NonEmptyString.unsafeFrom("synopsis")

      synopsisChain = LLMChain.make(
        llm = openAIClient,
        promptTemplate = promptTemplateS,
        llmModel = "davinci",
        user = "testing",
        echo = false,
        n = 1,
        temperature = 0.8,
        outputVariable = ouptuVariableS,
        onlyOutput = true
      )

      ouptuVariableR = NonEmptyString.unsafeFrom("review")

      reviewChain = LLMChain.make(
        llm = openAIClient,
        promptTemplate = promptTemplateR,
        llmModel = "davinci",
        user = "testing",
        echo = false,
        n = 1,
        temperature = 0.8,
        outputVariable = ouptuVariableR,
        onlyOutput = true
      )

      chains = NonEmptySeq(synopsisChain, Seq(reviewChain))

      inputKey = NonEmptyString.unsafeFrom("input")
      outputKey = NonEmptyString.unsafeFrom("output")

      ssc <- SimpleSequentialChain.make[IO](chains, inputKey, outputKey)
      response <- ssc.run("Terror at White Mountain Peak")
      _ = println(s"input: ${response(inputKey.toString)}")
      _ = println(s"output: ${response(outputKey.toString)}")
    yield ()
