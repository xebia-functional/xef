package com.xebia.functional.xef.scala.prompt

final case class Prompt(message: String) {
  def +(text: String): Prompt = Prompt(message + text)

  def +(other: Prompt): Prompt = Prompt(message + other.message)
}
object Prompt {

  extension (s: String) {
    def prompt(): Prompt = Prompt(s)
  }

  extension (p: Prompt) {
    def append(text: String) = p + text

    def prepend(text: String) = Prompt(text + p.message)
  }
}
