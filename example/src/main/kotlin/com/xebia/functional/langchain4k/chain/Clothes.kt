package com.xebia.functional.langchain4k.chain

import arrow.core.raise.either
import arrow.fx.coroutines.resourceScope
import com.xebia.functional.chains.VectorQAChain
import com.xebia.functional.embeddings.OpenAIEmbeddings
import com.xebia.functional.env.OpenAIConfig
import com.xebia.functional.llm.openai.KtorOpenAIClient
import com.xebia.functional.loaders.TextLoader
import com.xebia.functional.vectorstores.LocalVectorStore
import io.github.oshai.KotlinLogging
import okio.Path.Companion.toPath
import kotlin.time.ExperimentalTime

@PublishedApi
internal val logger = KotlinLogging.logger("Clothes")

@OptIn(ExperimentalTime::class)
suspend fun main() {
    resourceScope {
        either {
            val OPENAI_TOKEN = "<place-your-openai-token-here>"

            val openAIConfig = OpenAIConfig()
            val openAiClient = KtorOpenAIClient(openAIConfig)
            val embeddings = OpenAIEmbeddings(openAIConfig, openAiClient, logger)
            val vectorStore = LocalVectorStore(embeddings)

            val path = "/weather.txt".toPath()
            val textLoader = TextLoader(path)
            val docs = textLoader.load()
            vectorStore.addDocuments(docs)

            val outputVariable = "answer"
            val chain = VectorQAChain(
                openAiClient,
                vectorStore,
                1,
                outputVariable
            )

            chain.run("")
        }
    }


}