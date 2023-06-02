package com.xebia.functional.xef.scala.prompt

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder

trait PromptTemplate[A] {

  def chain(template: String): A

  extension (a: A) {
    def chain[B: SerialDescriptor: Decoder](template: A => String): B =
      ai(prompt[B](template(a)))
  }
}

object PromptTemplate:

  def apply[A](instance: A): A = instance

  inline final def derived[A: SerialDescriptor: Decoder]: PromptTemplate[A] = new PromptTemplate[A]:

    def chain(template: String): A = ai(prompt[A](template))
