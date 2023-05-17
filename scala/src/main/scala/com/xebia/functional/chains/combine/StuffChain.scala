package com.xebia.functional.scala.chains.combine

import cats.effect.Sync
import cats.syntax.all.*

import com.xebia.functional.scala.chains.LLMChain
import com.xebia.functional.scala.chains.models.Config
import com.xebia.functional.scala.domain.Document
import com.xebia.functional.scala.llm.LLM
import com.xebia.functional.scala.llm.models.LLMResult
import com.xebia.functional.scala.llm.openai.OpenAIClient
import com.xebia.functional.scala.prompt.PromptTemplate
import eu.timepit.refined.types.string.NonEmptyString

class StuffChain[F[_]: Sync](
    documents: List[Document],
    llm: LLM[F],
    promptTemplate: PromptTemplate[F],
    documentVariableName: String,
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
    val llmChain = LLMChain.make[F](llm, promptTemplate, outputVariable, onlyOutput)
    for
      documentInput <- combineDocs(documents)
      totalInputs = documentInput ++ inputs
      result <- llmChain.run(totalInputs)
    yield result

object StuffChain:
  def make[F[_]: Sync](
      documents: List[Document],
      llm: LLM[F],
      promptTemplate: PromptTemplate[F],
      documentVariableName: String,
      outputVariable: NonEmptyString,
      onlyOutput: Boolean
  ): StuffChain[F] =
    new StuffChain[F](
      documents,
      llm,
      promptTemplate,
      documentVariableName,
      outputVariable,
      onlyOutput
    )
