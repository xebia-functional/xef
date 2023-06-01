package com.xebia.functional.xef.scala.prompt

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder

trait PromptTemplate[A: ScalaSerialDescriptor: Decoder] {

  def create(template: String): A

  extension (a: A) {
    def add[B: ScalaSerialDescriptor: Decoder](template: A => String): B =
      ai(prompt[B](template(a)))
  }
}

object PromptTemplate:

  def apply[A](using ev: PromptTemplate[A]): PromptTemplate[A] = ev

  inline final def derived[A: ScalaSerialDescriptor: Decoder]: PromptTemplate[A] = new PromptTemplate[A]:
    def create(template: String): A = ai(prompt[A](template))
