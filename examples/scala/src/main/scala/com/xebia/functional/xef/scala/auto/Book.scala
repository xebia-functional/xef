package com.xebia.functional.xef.scala.conversation

import com.xebia.functional.xef.scala.conversation.Description
import com.xebia.functional.xef.scala.conversation.*
import io.circe.Decoder
import com.xebia.functional.xef.prompt.Prompt

@Description("A book")
case class Book(
    @Description("the name of the book") name: String,
    @Description("the author of the book") author: String,
    @Description("A 50 word paragraph with a summary of this book") summary: String
) derives SerialDescriptor,
      Decoder

def summarizeBook(title: String, author: String)(using conversation: ScalaConversation): Book =
  prompt(Prompt(s"$title by $author summary."))

@main def runBook: Unit =
  conversation {
    val toKillAMockingBird = summarizeBook("To Kill a Mockingbird", "Harper Lee")
    println(s"${toKillAMockingBird.name} by ${toKillAMockingBird.author} summary:\n ${toKillAMockingBird.summary}")
  }
