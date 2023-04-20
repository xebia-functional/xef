package com.xebia.functional.sinopsisreviewssc

import cats.effect.{IO, IOApp, Resource}
import com.xebia.functional.config.*

import scala.concurrent.duration._
import com.xebia.functional.chains.*
import com.xebia.functional.embeddings.openai.models.*
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.vectorstores.postgres.*
import eu.timepit.refined.types.string.NonEmptyString
import cats.data.NonEmptySeq
import com.xebia.functional.prompt.PromptTemplate

object QASimpleSequentialChain extends IOApp.Simple:
  override def run: IO[Unit] =
    val OPENAI_TOKEN = "<place-your-openai-token-here>"
    val openAIConfig = OpenAIConfig(OPENAI_TOKEN, 5.seconds, 5, 1000)
    lazy val openAIClient = OpenAIClient[IO](openAIConfig)

    val sinopsisTempl = """
    You are a playwright. Given a title of play, it is your job to write a sinopsis for that title.

    Title: {title}
    Playwright: This is a synopsis for the above play:
    """

    val reviewTempl = """
    You are a play critic from the New York Times. Given the sinopsis of play, it is you job to write a review for that play.

    Play Sinopsis: {synopsis}
    Review from a New York Times play critic of the above play:
    """

    for
      promptTemplateS <- PromptTemplate.fromTemplate[IO](template = sinopsisTempl, inputVariables = List("title"))
      promptTemplateR <- PromptTemplate.fromTemplate[IO](template = reviewTempl, inputVariables = List("synopsis"))

      synopsisChain = LLMChain.make(
        llm = openAIClient,
        promptTemplate = promptTemplateS,
        llmModel = "davinci",
        user = "testing",
        echo = false,
        n = 1,
        temperature = 0.5,
        onlyOutput = true
      )

      reviewChain = LLMChain.make(
        llm = openAIClient,
        promptTemplate = promptTemplateR,
        llmModel = "davinci",
        user = "testing",
        echo = false,
        n = 1,
        temperature = 0.5,
        onlyOutput = true
      )

      chains = NonEmptySeq(synopsisChain, Seq(reviewChain))

      inputKey = NonEmptyString.from("input").toOption.get
      outputKey = NonEmptyString.from("output").toOption.get

      ssc <- SimpleSequentialChain.make[IO](chains, inputKey, outputKey)
      response <- ssc.run("A beautiful day in the mountains ends up turning into a nightmare for a group of friends.")
      _ = println(response)
    yield ()
