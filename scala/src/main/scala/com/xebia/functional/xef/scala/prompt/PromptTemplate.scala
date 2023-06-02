package com.xebia.functional.xef.scala.prompt

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder

trait PromptTemplate[A: ScalaSerialDescriptor: Decoder] {

  def chain(template: String): A

  extension (a: A) {
    def chain[B: ScalaSerialDescriptor: Decoder](template: A => String): B =
      ai(prompt[B](template(a)))
  }
}

object PromptTemplate:

  def apply[A](using ev: PromptTemplate[A]): PromptTemplate[A] = ev

  inline final def derived[A: ScalaSerialDescriptor: Decoder]: PromptTemplate[A] = new PromptTemplate[A]:
    def chain(template: String): A = ai(prompt[A](template))
