package com.xebia.functional.scala.llm.openai.models

import java.util.{List => JList}

import scala.jdk.CollectionConverters._

import com.theokanning.openai.embedding.{EmbeddingRequest => JEmbeddingRequest}

final case class EmbeddingRequest(model: String, input: List[String], user: String)

object EmbeddingRequest:

  extension (e: EmbeddingRequest)
    def asJava: JEmbeddingRequest =
      new JEmbeddingRequest(
        e.model,
        e.input.asJava,
        e.user
      )
