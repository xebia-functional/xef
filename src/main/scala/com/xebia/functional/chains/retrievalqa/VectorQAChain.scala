package com.xebia.functional.chains.retrievalqa

import cats.effect.Sync
import cats.syntax.all.*

import com.xebia.functional.chains.combine.CombineDocumentsChain
import com.xebia.functional.chains.combine.StuffChain
import com.xebia.functional.chains.models.CombineDocumentsChainType
import com.xebia.functional.chains.models.CombineDocumentsChainType.Stuff
import com.xebia.functional.chains.models.Config
import com.xebia.functional.chains.models.InvalidChainInputsError
import com.xebia.functional.domain.Document
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.prompt.PromptTemplate
import com.xebia.functional.vectorstores.VectorStore

class VectorQAChain[F[_]: Sync](
    llm: OpenAIClient[F],
    vectorStore: VectorStore[F],
    chainType: String,
    numberOfDocs: Int,
    llmModel: String,
    user: String,
    echo: Boolean,
    n: Int,
    temperature: Double,
    onlyOutput: Boolean
) extends RetrievalQAChain[F]:
  val documentVariableName: String = "context"
  val inputVariable: String = "question"
  val config = Config(Set(inputVariable), Set("answer"), onlyOutput)
  val promptTemplate: F[PromptTemplate[F]] = QAPrompt.promptTemplate[F]

  def loadCombineDocsChain(
      chain: CombineDocumentsChainType,
      documents: List[Document],
      prompt: PromptTemplate[F]
  ): CombineDocumentsChain[F] =
    chain match
      case Stuff => StuffChain.make[F](documents, llm, prompt, documentVariableName, llmModel, user, echo, n, temperature, onlyOutput)

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

  def run(inputs: Map[String, String] | String): F[Map[String, String]] =
    for
      is <- prepareInputs(inputs)(config)
      os <- call(is)
      output <- prepareOutputs(is, os)(config)
    yield output

object VectorQAChain:
  def make[F[_]: Sync](
      llm: OpenAIClient[F],
      vectorStore: VectorStore[F],
      chainType: String,
      numberOfDocs: Int,
      llmModel: String,
      user: String,
      echo: Boolean,
      n: Int,
      temperature: Double,
      onlyOutput: Boolean
  ): VectorQAChain[F] =
    new VectorQAChain[F](llm, vectorStore, chainType, numberOfDocs, llmModel, user, echo, n, temperature, onlyOutput)

  def makeWithDefaults[F[_]: Sync](llm: OpenAIClient[F], vectorStore: VectorStore[F], user: String): VectorQAChain[F] =
    new VectorQAChain[F](
      llm,
      vectorStore,
      chainType = "stuff",
      numberOfDocs = 10,
      llmModel = "text-davinci-003",
      user = user,
      echo = false,
      n = 1,
      temperature = 0.0,
      onlyOutput = true
    )
