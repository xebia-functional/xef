package com.xebia.functional.textsplitters

import scala.util.control.NoStackTrace

class SplitterError(reason: String) extends Throwable with NoStackTrace:
  override def getMessage: String = s"Error while splitting documents: $reason"
