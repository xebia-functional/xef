package com.xebia.functional.llm.huggingface.models

import scala.util.control.NoStackTrace

class HuggingFaceError(reason: String) extends Throwable with NoStackTrace:
  override def getMessage(): String = s"Error communicating with HuggingFace: `$reason`"
