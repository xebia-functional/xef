package com.xebia.functional.scala.loaders

import scala.util.control.NoStackTrace

class LoaderError(reason: String) extends Throwable with NoStackTrace:
  override def getMessage: String = s"Error while uploading documents: $reason"
