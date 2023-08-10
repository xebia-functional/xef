package com.xebia.functional.xef.reasoning.tools

interface ToolWikipedia : ToolMain {
  suspend operator fun invoke(input: String?, pageId: Int?, title: String?): String
}
