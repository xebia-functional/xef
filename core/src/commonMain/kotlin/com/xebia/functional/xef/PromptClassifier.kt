package com.xebia.functional.xef

interface PromptClassifier {
  fun template(input: String, output: String, context: String): String
}
