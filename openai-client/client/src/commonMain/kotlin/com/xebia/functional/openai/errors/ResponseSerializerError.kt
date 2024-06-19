package com.xebia.functional.openai.errors

class ResponseSerializerError(message: String, cause: Throwable? = null) :
  Exception(message, cause)
