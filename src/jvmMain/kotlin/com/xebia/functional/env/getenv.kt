package com.xebia.functional.env

actual fun getenv(name: String): String? =
  System.getenv(name)
