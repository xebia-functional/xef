package com.xebia.functional.chains

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.recover
import com.xebia.functional.AIError
import com.xebia.functional.AIError.Chain.InvalidTemplate
import com.xebia.functional.llm.openai.LLMModel
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.prompt.PromptTemplate
import com.xebia.functional.vectorstores.VectorStore

interface VectorQAChain : Chain {
  suspend fun getDocs(question: String): List<String>
}

@Suppress("LongParameterList")
suspend fun VectorQAChain(
  llm: OpenAIClient,
  llmModel: LLMModel,
  vectorStore: VectorStore,
  numOfDocs: Int,
  outputVariable: String,
  chainOutput: Chain.ChainOutput = Chain.ChainOutput.OnlyOutput
): VectorQAChain =
  object : VectorQAChain {

    private val documentVariableName: String = "context"
    private val inputVariable: String = "question"

    override val config: Chain.Config =
      Chain.Config(setOf(inputVariable), setOf(outputVariable), chainOutput)

    override suspend fun getDocs(question: String): List<String> =
      vectorStore.similaritySearch(question, numOfDocs)

    override suspend fun call(
      inputs: Map<String, String>
    ): Either<AIError.Chain, Map<String, String>> = either {
      val promptTemplate = promptTemplate()

      val question = validateInput(inputs, inputVariable)
      val documents = getDocs(question)

      val chain =
        CombineDocsChain(
          llm,
          promptTemplate,
          llmModel,
          documents,
          documentVariableName,
          outputVariable,
          chainOutput
        )

      chain.run(inputs).bind()
    }

    private fun Raise<InvalidTemplate>.promptTemplate(): PromptTemplate<String> =
      recover({
        val template =
          """
                |Use the following pieces of context to answer the question at the end. If you don't know the answer, just say that you don't know, don't try to make up an answer.
                |
                |{context}
                |
                |Question: {question}
                |Helpful Answer:"""
            .trimMargin()

        PromptTemplate(template, listOf("context", "question"))
      }) {
        raise(InvalidTemplate(it.reason))
      }
  }
