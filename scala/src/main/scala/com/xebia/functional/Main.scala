import scala.jdk.CollectionConverters.*

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.*
import cats.implicits.*
import cats.syntax.all.*

import com.xebia.functional.scala.config.Config
import com.xebia.functional.scala.llm.huggingface.*
import com.xebia.functional.scala.llm.huggingface.models.Model
import com.xebia.functional.scala.llm.models.HFRequest
import com.xebia.functional.scala.llm.models.OpenAIRequest
import com.xebia.functional.scala.llm.openai.OpenAIClient
import com.xebia.functional.scala.llm.openai.models.EmbeddingRequest
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.implicits.uri

object Main extends IOApp.Simple {
  def run: IO[Unit] =
    (
      for
        httpClient <- EmberClientBuilder.default[IO].build
        config <- Resource.eval(Config.configValue[IO].load[IO])
        openAIClient = OpenAIClient[IO](config.openAI)
        hfClient = HuggingFaceClient[IO](config.huggingFace, httpClient)
      yield (openAIClient, hfClient)
    ).use { case (oai, hf) =>
      for
        o1 <- openAIExample(oai)
        o2 <- hfExample(hf)
        o3 <- openAIEmbeddingsExample(oai)
        _ = println(o1)
        _ = println(o2)
        _ = println(o3)
      yield ()
    }

  def openAIEmbeddingsExample(client: OpenAIClient[IO]) =
    client
      .createEmbeddings(
        EmbeddingRequest(model = "text-embedding-ada-002", input = List("How much is 2+2"), user = "testing")
      )

  def openAIExample(client: OpenAIClient[IO]) =
    client
      .generate(
        OpenAIRequest
          .builder(model = "ada", user = "testing")
          .withPrompt("Write a tagline for an ice cream shop.")
          .withEcho(true)
          .withN(3)
          .build()
      )

  def hfExample(client: HuggingFaceClient[IO]) =
    client
      .generate(
        HFRequest("Write a tagline for an ice cream shop.")
      )
}
