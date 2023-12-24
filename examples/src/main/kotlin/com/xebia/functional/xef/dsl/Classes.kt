package com.xebia.functional.xef.dsl

import com.xebia.functional.xef.AI
import kotlinx.serialization.Serializable

@Serializable data class Book(val title: String, val author: String)

suspend fun main() {
  val book = AI<Book>("A book about hobbits and rings")
  println(book) // Book(title=The Hobbit, author=J.R.R. Tolkien)
}
