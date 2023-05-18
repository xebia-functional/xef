package com.xebia.functional.xef.pdf

import arrow.fx.coroutines.resourceScope
import com.xebia.functional.xef.agents.ParameterlessAgent
import com.xebia.functional.xef.llm.openai.LLMModel
import com.xebia.functional.xef.loaders.BaseLoader
import com.xebia.functional.xef.textsplitters.BaseTextSplitter
import com.xebia.functional.xef.textsplitters.TokenTextSplitter
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File

suspend fun pdf(
  url: String,
  splitter: BaseTextSplitter = TokenTextSplitter(modelName = LLMModel.GPT_3_5_TURBO.name, chunkSize = 100, chunkOverlap = 50)
): ParameterlessAgent<List<String>> =
  resourceScope {
    val client = install({
       HttpClient { }
    }) { client, _ ->
      client.close()
    }
    val response = client.get(url)
    val file = File.createTempFile("pdf", ".pdf")
    file.writeChannel().use {
      response.bodyAsChannel().copyAndClose(this)
    }
    pdf(file, splitter)
  }


fun pdf(
  file: File,
  splitter: BaseTextSplitter = TokenTextSplitter(modelName = LLMModel.GPT_3_5_TURBO.name, chunkSize = 100, chunkOverlap = 50)
): ParameterlessAgent<List<String>> =
  ParameterlessAgent(name = "Get PDF content", description = "Get PDF Content of $file") {
    val loader = PDFLoader(file)
    loader.loadAndSplit(splitter)
  }

class PDFLoader(private val file: File) : BaseLoader {
  override suspend fun load(): List<String> {
    val doc = PDDocument.load(file)
    return doc.use {
      val stripper = PDFTextStripper()
      stripper.sortByPosition = true
      listOf(
        """|
      |Title: ${it.documentInformation.title}
      |Info: ${stripper.getText(doc)}
    """.trimMargin()
      )
    }
  }
}
