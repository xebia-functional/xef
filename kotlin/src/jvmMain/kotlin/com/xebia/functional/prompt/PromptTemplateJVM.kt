package com.xebia.functional.prompt

import arrow.core.raise.Raise
import okio.FileSystem
import okio.Path

suspend fun Raise<InvalidTemplate>.PromptTemplate(
  templateFile: Path,
  inputVariables: List<String>
): PromptTemplate =
  PromptTemplate(templateFile, inputVariables, FileSystem.SYSTEM)
