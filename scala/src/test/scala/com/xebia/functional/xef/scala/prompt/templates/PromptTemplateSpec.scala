package com.xebia.functional.xef.scala.prompt.templates

import munit.FunSuite

class PromptTemplateSpec extends FunSuite:
  import com.xebia.functional.xef.scala.prompt.templates.*

  test("Should create prompt template for lists") {
    assert("Colours".writeListOf().startsWith("Write a list of Colours in the following format"))
  }

  test("Should create prompt template for sequences") {
    assert("Titles".writeSequenceOf().startsWith("Write a sequence of Titles in the following format"))
  }
