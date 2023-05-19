package com.xebia.functional.xef.pdf

import com.xebia.functional.xef.llm.openai.LLMModel
import com.xebia.functional.xef.loaders.BaseLoader
import com.xebia.functional.xef.textsplitters.BaseTextSplitter
import com.xebia.functional.xef.textsplitters.TokenTextSplitter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File

suspend fun pdf(
  file: File,
  splitter: BaseTextSplitter = TokenTextSplitter(modelName = LLMModel.GPT_3_5_TURBO.name, chunkSize = 100, chunkOverlap = 50)
): List<String> {
  val loader = PDFLoader(file)
  return loader.loadAndSplit(splitter)
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
