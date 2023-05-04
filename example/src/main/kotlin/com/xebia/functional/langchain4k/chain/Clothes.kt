package com.xebia.functional.langchain4k.chain

import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.recover
import arrow.fx.coroutines.resourceScope
import com.xebia.functional.Document
import com.xebia.functional.chains.VectorQAChain
import com.xebia.functional.embeddings.OpenAIEmbeddings
import com.xebia.functional.env.OpenAIConfig
import com.xebia.functional.llm.openai.KtorOpenAIClient
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.loaders.BaseLoader
import com.xebia.functional.loaders.TextLoader
import com.xebia.functional.vectorstores.LocalVectorStore
import io.github.oshai.KotlinLogging
import okio.Path
import okio.Path.Companion.toPath
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.ExperimentalTime

@PublishedApi
internal val logger = KotlinLogging.logger("Clothes")

data class Error(val reason: String)

@OptIn(ExperimentalTime::class)
suspend fun main() {
    resourceScope {
        either {
            val openAIConfig = recover({
                val token = "<OPENAI_TOKEN>"
                OpenAIConfig(token)
            }) { raise(Error(it.joinToString(", "))) }

            val openAiClient: OpenAIClient = KtorOpenAIClient(openAIConfig)
            val embeddings = OpenAIEmbeddings(openAIConfig, openAiClient, logger)
            val vectorStore = LocalVectorStore(embeddings)

            val resource: URL = javaClass.getResource("/documents/weather.txt") ?: raise(Error("Resource not found"))
            val path: Path = File(resource.file).path.toPath()

            val textLoader: BaseLoader = TextLoader(path)
            val docs: List<Document> = textLoader.load()
            vectorStore.addDocuments(docs)

            val numOfDocs = 10
            val outputVariable = "answer"
            val chain = VectorQAChain(
                openAiClient,
                vectorStore,
                numOfDocs,
                outputVariable
            )

            val sdf = SimpleDateFormat("dd/MM/yyyy")
            val currentDate: String = sdf.format(Date()) ?: raise(Error("Invalid date"))

            val question = "If today is $currentDate, what clothes do you recommend should I wear this week?"
            val response: Map<String, String> = chain.run(question).getOrElse { raise(Error(it.reason)) }

            println(response)

        }.getOrElse { throw IllegalStateException(it.reason) }
    }
}
