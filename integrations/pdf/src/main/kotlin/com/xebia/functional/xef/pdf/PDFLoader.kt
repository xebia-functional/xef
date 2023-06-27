package com.xebia.functional.xef.pdf

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.loaders.BaseLoader
import com.xebia.functional.xef.textsplitters.TextSplitter
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
  splitter: TextSplitter = TokenTextSplitter(modelType = ModelType.GPT_3_5_TURBO, chunkSize = 100, chunkOverlap = 50)
): List<String> =
  HttpClient().use {
    val response = it.get(url)
    val file = File.createTempFile("pdf", ".pdf")
    file.writeChannel().use {
      response.bodyAsChannel().copyAndClose(this)
    }
    pdf(file, splitter)
  }


suspend fun pdf(
  file: File,
  splitter: TextSplitter = TokenTextSplitter(modelType = ModelType.GPT_3_5_TURBO, chunkSize = 100, chunkOverlap = 50)
): List<String> {
  val loader = PDFLoader(file)
  return loader.loadAndSplit(splitter)
}

private class PDFLoader(private val file: File) : BaseLoader {
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
