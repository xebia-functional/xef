package com.xebia.functional.scala.chains

import cats.effect.IO
import cats.effect.kernel.Ref

import com.xebia.functional.scala.domain.Document

object TestData {

  val contextFormatted = """foo foo foo
  |bar bar bar
  |baz baz baz""".stripMargin

  val contextOutput = Map("context" -> contextFormatted)
  val template = """From the following context:
    |
    |{context}
    |
    |try to answer the following question: {question}""".stripMargin

  val templateInputs = """From the following context:
    |
    |{context}
    |
    |I want to say: My name is {name} and I'm {age} years old""".stripMargin

  val templateFormatted = """From the following context:
    |
    |foo foo foo
    |bar bar bar
    |baz baz baz
    |
    |try to answer the following question: What do you think?""".stripMargin

  val templateInputsFormatted = """From the following context:
    |
    |foo foo foo
    |bar bar bar
    |baz baz baz
    |
    |I want to say: My name is Scala and I'm 28 years old""".stripMargin

  val qaTemplateFormatted = """
    |Use the following pieces of context to answer the question at the end. If you don't know the answer, just say that you don't know, don't try to make up an answer.
    |
    |foo foo foo
    |bar bar bar
    |baz baz baz
    |
    |Question: What do you think?
    |Helpful Answer:""".stripMargin

  val outputIDK = Map("answer" -> "I don't know")
  val outputInputs = Map("answer" -> "Two inputs, right?")

  val docsList = Map(
    memeid4s.UUID.V1.next.asJava() -> Document("foo foo foo"),
    memeid4s.UUID.V1.next.asJava() -> Document("bar bar bar"),
    memeid4s.UUID.V1.next.asJava() -> Document("baz baz baz")
  )

  val ref = Ref[IO].of(docsList)

}
