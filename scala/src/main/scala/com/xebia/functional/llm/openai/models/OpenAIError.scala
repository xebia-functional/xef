package com.xebia.functional.llm.openai.models

import scala.util.control.NoStackTrace

class OpenAIError(reason: String) extends Throwable with NoStackTrace:
  override def getMessage(): String = s"Error communicating with OpenAI: `$reason`"
