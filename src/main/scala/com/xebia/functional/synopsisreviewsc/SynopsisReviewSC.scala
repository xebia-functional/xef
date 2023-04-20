package com.xebia.functional.synopsisreviewsc

import cats.effect.{IO, IOApp}
import cats.implicits.*
import com.xebia.functional.config.*

import scala.concurrent.duration._
import com.xebia.functional.chains.*
import com.xebia.functional.embeddings.openai.models.*
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.vectorstores.postgres.*
import eu.timepit.refined.types.string.NonEmptyString
import cats.data.{NonEmptySeq, NonEmptySet}
import com.xebia.functional.prompt.PromptTemplate
import cats.kernel.Order

object SynopsisReviewSC extends IOApp.Simple:

  given Order[NonEmptyString] = Order.fromComparable[String].contramap(_.value)
  override def run: IO[Unit] =
    val OPENAI_TOKEN = ""
    val openAIConfig = OpenAIConfig(OPENAI_TOKEN, 5.seconds, 5, 1000)
    lazy val openAIClient = OpenAIClient[IO](openAIConfig)

    val sinopsisTempl = """
    |You are a playwright. Given the title of play and the era it is set in, it is your job to write a synopsis for that title.
    |
    |Title: {title}
    |Era: {era}
    |Playwright: This is a synopsis for the above play:
    """.stripMargin.replace("\n", " ")

    val reviewTempl = """
    |You are a play critic from the New York Times. Given the synopsis of play, it is your job to write a review for that play.
    |
    |Play Sinopsis: {synopsis}
    |Review from a New York Times play critic of the above play:
    """.stripMargin.replace("\n", " ")

    for
      promptTemplateS <- PromptTemplate.fromTemplate[IO](template = sinopsisTempl, inputVariables = List("title", "era"))
      promptTemplateR <- PromptTemplate.fromTemplate[IO](template = reviewTempl, inputVariables = List("synopsis"))

      outputVariableS = NonEmptyString.unsafeFrom("synopsis")

      synopsisChain = LLMChain.make(
        llm = openAIClient,
        promptTemplate = promptTemplateS,
        llmModel = "davinci",
        user = "testing",
        echo = false,
        n = 1,
        temperature = 0.8,
        outputVariable = outputVariableS,
        onlyOutput = true
      )

      outputVariableR = NonEmptyString.unsafeFrom("review")

      reviewChain = LLMChain.make(
        llm = openAIClient,
        promptTemplate = promptTemplateR,
        llmModel = "davinci",
        user = "testing",
        echo = false,
        n = 1,
        temperature = 0.8,
        outputVariable = outputVariableR,
        onlyOutput = true
      )

      chains = NonEmptySeq(synopsisChain, Seq(reviewChain))

      inputKey0 = NonEmptyString.unsafeFrom("title")
      inputKey1 = NonEmptyString.unsafeFrom("era")
      outputKey = NonEmptyString.unsafeFrom("review")

      ssc <- SequentialChain.make[IO](chains, NonEmptySet.of(inputKey0, inputKey1), NonEmptySet.of(outputKey))
      response <- ssc.run(Map("title" -> "Coward Heart", "era" -> "middle age"))
      _ = println(s"title: ${response(inputKey0.toString)}")
      _ = println(s"era: ${response(inputKey1.toString)}")
      _ = println(s"review: ${response(outputKey.toString)}")
    yield ()
