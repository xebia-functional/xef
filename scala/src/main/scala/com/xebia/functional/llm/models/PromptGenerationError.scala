package com.xebia.functional.llm.models

import scala.util.control.NoStackTrace

class PromptGenerationError(reason: String) extends Throwable with NoStackTrace:
  override def getMessage(): String = reason
