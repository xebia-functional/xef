package com.xebia.functional.synopsisreviewsc

import scala.concurrent.duration._

import cats.data.NonEmptySeq
import cats.data.NonEmptySet
import cats.effect.IO
import cats.effect.IOApp
import cats.implicits.*
import cats.kernel.Order

import com.xebia.functional.chains.*
import com.xebia.functional.config.*
import com.xebia.functional.embeddings.openai.models.*
import com.xebia.functional.llm.LLM
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.prompt.PromptTemplate
import com.xebia.functional.vectorstores.postgres.*
import eu.timepit.refined.types.string.NonEmptyString

object SynopsisReviewSC extends IOApp.Simple:

  given Order[NonEmptyString] = Order.fromComparable[String].contramap(_.value)
  override def run: IO[Unit] =
    val OPENAI_TOKEN = ""
    val openAIConfig = OpenAIConfig(OPENAI_TOKEN, 5.seconds, 5, 1000, OpenAIConfigLLM(maxTokens = Some(500), temperature = Some(0.8)))
    lazy val openAIClient = OpenAIClient[IO](openAIConfig)

    val sinopsisTempl = """
    |You are a playwright. Given the title of play and the era it is set in, it is your job to write a synopsis for that title.
    |
    |Title: {title}.
    |Era: {era}.
    |Playwright: This is a synopsis for the above play:
    """.stripMargin.replace("\n", " ")

    val reviewTempl = """
    |You are a play critic from the New York Times. Given the synopsis of play, it is your job to write a review for that play.
    |
    |Play Sinopsis: {synopsis}.
    |Review from a New York Times play critic of the above play:
    """.stripMargin.replace("\n", " ")

    for
      promptTemplateS <- PromptTemplate.fromTemplate[IO](template = sinopsisTempl, inputVariables = List("title", "era"))
      promptTemplateR <- PromptTemplate.fromTemplate[IO](template = reviewTempl, inputVariables = List("synopsis"))

      outputVariableS <- NonEmptyString.from("synopsis").toOption.liftTo[IO](new RuntimeException("synopsis variable is empty"))

      synopsisChain = LLMChain.make(
        llm = LLM.openAI[IO](openAIClient),
        promptTemplate = promptTemplateS,
        outputVariable = outputVariableS,
        onlyOutput = false
      )

      outputVariableR <- NonEmptyString.from("review").toOption.liftTo[IO](new RuntimeException("review variable is empty"))

      reviewChain = LLMChain.make(
        llm = LLM.openAI[IO](openAIClient),
        promptTemplate = promptTemplateR,
        outputVariable = outputVariableR,
        onlyOutput = false
      )

      chains = NonEmptySeq(synopsisChain, Seq(reviewChain))

      inputKey0 <- NonEmptyString.from("title").toOption.liftTo[IO](new RuntimeException("title variable is empty"))
      inputKey1 <- NonEmptyString.from("era").toOption.liftTo[IO](new RuntimeException("era variable is empty"))
      outputKey0 <- NonEmptyString.from("synopsis").toOption.liftTo[IO](new RuntimeException("synopsis variable is empty"))
      outputKey1 <- NonEmptyString.from("review").toOption.liftTo[IO](new RuntimeException("review variable is empty"))

      ssc <- SequentialChain.make[IO](chains, NonEmptySet.of(inputKey0, inputKey1), NonEmptySet.of(outputKey0, outputKey1))
      response <- ssc.run(Map("title" -> "The power of Zuluastral", "era" -> "Modern Era"))
      _ = println(s"Title: ${response(inputKey0.toString)}\n")
      _ = println(s"Era: ${response(inputKey1.toString)}\n")
      _ = println(s"Synopsis: ${response(outputKey0.toString)}\n")
      _ = println(s"Review: ${response(outputKey1.toString)}\n")
    yield ()
