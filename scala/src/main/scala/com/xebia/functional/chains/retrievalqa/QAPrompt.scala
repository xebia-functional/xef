package com.xebia.functional.scala.chains.retrievalqa

import cats.effect.Sync

import com.xebia.functional.scala.prompt.PromptTemplate

object QAPrompt {

  val template = """
    |Use the following pieces of context to answer the question at the end. If you don't know the answer, just say that you don't know, don't try to make up an answer.
    |
    |{context}
    |
    |Question: {question}
    |Helpful Answer:""".stripMargin

  def promptTemplate[F[_]: Sync] = PromptTemplate.fromTemplate[F](template, List("context", "question"))

}
