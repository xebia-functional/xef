package com.xebia.functional.xef.auto.manual

import com.xebia.functional.gpt4all.HuggingFaceLocalEmbeddings
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.pdf.pdf
import com.xebia.functional.xef.vectorstores.LocalVectorStore

suspend fun main() {
  val chat = OpenAI.DEFAULT_CHAT
  val huggingFaceEmbeddings = HuggingFaceLocalEmbeddings.DEFAULT
  val vectorStore = LocalVectorStore(huggingFaceEmbeddings)
  val results = pdf("https://www.europarl.europa.eu/RegData/etudes/STUD/2023/740063/IPOL_STU(2023)740063_EN.pdf")
  vectorStore.addTexts(results)
  val result: List<String> = chat.promptMessage("What is the content about?", vectorStore)
  println(result)
}
