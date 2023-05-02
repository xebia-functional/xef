package com.xebia.functional.llm.openai.models

import scala.jdk.CollectionConverters._

import com.theokanning.openai.embedding.{Embedding => JEmbedding}
import com.theokanning.openai.embedding.{EmbeddingResult => JEmbeddingResult}

final case class EmbeddingResult(
    model: String,
    `object`: String,
    data: Vector[EmbeddingResult.Embedding],
    usage: Usage
)

object EmbeddingResult:

  def fromJava(j: JEmbeddingResult): EmbeddingResult =
    EmbeddingResult(j.getModel(), j.getObject(), j.getData().asScala.toVector.map(Embedding.fromJava), Usage.fromJava(j.getUsage()))

  final case class Embedding(objectVal: String, embedding: Vector[Double], index: Integer)

  object Embedding:
    def fromJava(j: JEmbedding): Embedding =
      Embedding(j.getObject(), j.getEmbedding().asScala.toVector.map(_.doubleValue), j.getIndex())
