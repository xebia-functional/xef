package com.xebia.functional.xef.env

actual fun getenv(name: String): String? = System.getenv(name)
