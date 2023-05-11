package com.xebia.functional.scala.prompt.models

import scala.util.control.NoStackTrace

class InvalidTemplateError(reason: String) extends Throwable with NoStackTrace:
  override def getMessage(): String = reason
