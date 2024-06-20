package com.xebia.functional.xef.vectorstore

import com.xebia.functional.xef.OpenAI
import com.xebia.functional.xef.store.LocalVectorStore
import com.xebia.functional.xef.store.VectorStore.Document

suspend fun main() {
  val embeddings = OpenAI().embeddings
  val vectorStore = LocalVectorStore(embeddings)
  val helloDoc = Document("Hello, how are you?", "source1")
  val unrelatedDoc = Document("Unrelated text", "source2")
  vectorStore.addDocuments(listOf(helloDoc, unrelatedDoc))
  val maybeHelloDoc = vectorStore.similaritySearch("Hello", 1).first()
  assert(maybeHelloDoc == helloDoc) { "Expected $helloDoc but got $maybeHelloDoc" }
  val maybeUnrelatedDoc = vectorStore.similaritySearch("Unrelated", 1).first()
  assert(maybeUnrelatedDoc == unrelatedDoc) { "Expected $unrelatedDoc but got $maybeUnrelatedDoc" }
  println("All expected documents found!")
}
