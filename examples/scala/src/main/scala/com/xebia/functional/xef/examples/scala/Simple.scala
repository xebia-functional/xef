package com.xebia.functional.xef.examples.scala

import com.xebia.functional.xef.scala.conversation.*

@main def runBooks(): Unit = conversation:
  val topic = "functional programming"
  val topBook: String = promptMessage(s"Give me the top-selling book about $topic")
  println(topBook)
  val selectedBooks: List[String] = promptMessages(s"Give me a selection of books about $topic")
  println(selectedBooks.mkString("\n"))
