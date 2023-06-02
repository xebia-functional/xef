package com.xebia.functional.xef.scala.prompt

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder

trait PromptTemplate[A] {

  def chain[B: ScalaSerialDescriptor: Decoder](template: String): B

  extension (a: A) {
    def chain[B: ScalaSerialDescriptor: Decoder](template: A => String): B =
      ai(prompt[B](template(a)))
  }
}

object PromptTemplate:

  def apply[A](instance: A): A = instance

  inline final def derived[A: ScalaSerialDescriptor: Decoder]: PromptTemplate[A] = new PromptTemplate[A]:

    def chain[B: ScalaSerialDescriptor: Decoder](template: String): B = ai(prompt[B](template))
