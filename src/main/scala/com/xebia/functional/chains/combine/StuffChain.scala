package com.xebia.functional.chains.combine

import cats.effect.Sync
import cats.syntax.all.*

import com.xebia.functional.chains.LLMChain
import com.xebia.functional.chains.models.Config
import com.xebia.functional.domain.Document
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.prompt.PromptTemplate
import eu.timepit.refined.types.string.NonEmptyString

class StuffChain[F[_]: Sync](
    documents: List[Document],
    llm: OpenAIClient[F],
    promptTemplate: PromptTemplate[F],
    documentVariableName: String,
    llmModel: String,
    user: String,
    echo: Boolean,
    n: Int,
    temperature: Double,
    outputVariable: NonEmptyString,
    onlyOutput: Boolean
) extends CombineDocumentsChain[F]:
  val config = Config(promptTemplate.inputKeys.toSet -- Set(documentVariableName), Set("answer"), onlyOutput)

  def mergeDocs(documents: List[Document]): String =
    documents.map(_.content).mkString("\n")

  def combineDocs(documents: List[Document]): F[Map[String, String]] =
    Sync[F].pure {
      val context = mergeDocs(documents)
      Map(documentVariableName -> context)
    }

  def call(inputs: Map[String, String]): F[Map[String, String]] =
    val llmChain = LLMChain.make[F](llm, promptTemplate, llmModel, user, echo, n, temperature, outputVariable, onlyOutput)
    for
      documentInput <- combineDocs(documents)
      totalInputs = documentInput ++ inputs
      result <- llmChain.run(totalInputs)
    yield result

object StuffChain:
  def make[F[_]: Sync](
      documents: List[Document],
      llm: OpenAIClient[F],
      promptTemplate: PromptTemplate[F],
      documentVariableName: String,
      llmModel: String,
      user: String,
      echo: Boolean,
      n: Int,
      temperature: Double,
      outputVariable: NonEmptyString,
      onlyOutput: Boolean
  ): StuffChain[F] =
    new StuffChain[F](documents, llm, promptTemplate, documentVariableName, llmModel, user, echo, n, temperature, outputVariable, onlyOutput)
