package com.xebia.functional.scala.chains.retrievalqa

import cats.effect.Sync
import cats.effect.SyncIO
import cats.syntax.all.*

import com.xebia.functional.scala.chains.combine.CombineDocumentsChain
import com.xebia.functional.scala.chains.combine.StuffChain
import com.xebia.functional.scala.chains.models.CombineDocumentsChainType
import com.xebia.functional.scala.chains.models.CombineDocumentsChainType.Stuff
import com.xebia.functional.scala.chains.models.Config
import com.xebia.functional.scala.chains.models.InvalidChainInputsError
import com.xebia.functional.scala.domain.Document
import com.xebia.functional.scala.llm.LLM
import com.xebia.functional.scala.llm.models.LLMResult
import com.xebia.functional.scala.llm.openai.OpenAIClient
import com.xebia.functional.scala.prompt.PromptTemplate
import com.xebia.functional.scala.vectorstores.VectorStore
import eu.timepit.refined.types.string.NonEmptyString

class VectorQAChain[F[_]: Sync](
    llm: LLM[F],
    vectorStore: VectorStore[F],
    chainType: String,
    numberOfDocs: Int,
    outputVariable: NonEmptyString,
    onlyOutput: Boolean
) extends RetrievalQAChain[F]:
  val documentVariableName: String = "context"
  val inputVariable: String = "question"
  val config = Config(Set(inputVariable), Set(outputVariable.value), onlyOutput)
  val promptTemplate: F[PromptTemplate[F]] = QAPrompt.promptTemplate[F]

  def loadCombineDocsChain(
      chain: CombineDocumentsChainType,
      documents: List[Document],
      prompt: PromptTemplate[F]
  ): CombineDocumentsChain[F] =
    chain match
      case Stuff =>
        StuffChain.make[F](documents, llm, prompt, documentVariableName, outputVariable, onlyOutput)

  def getDocs(question: String): F[List[Document]] =
    vectorStore.similaritySearch(question, numberOfDocs)

  def call(inputs: Map[String, String]): F[Map[String, String]] =
    for
      cct <- CombineDocumentsChainType.fromString[F](chainType)
      prt <- promptTemplate
      question <- inputs.get(inputVariable).liftTo[F](InvalidChainInputsError(config.inputKeys, inputs))
      documents <- getDocs(question)
      combineChain = loadCombineDocsChain(cct, documents, prt)
      output <- combineChain.run(inputs)
    yield output

object VectorQAChain:
  def make[F[_]: Sync](
      llm: LLM[F],
      vectorStore: VectorStore[F],
      chainType: String,
      numberOfDocs: Int,
      outputVariable: NonEmptyString,
      onlyOutput: Boolean
  ): VectorQAChain[F] =
    new VectorQAChain[F](llm, vectorStore, chainType, numberOfDocs, outputVariable, onlyOutput)
