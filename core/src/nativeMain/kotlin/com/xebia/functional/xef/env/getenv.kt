package com.xebia.functional.xef.env

import kotlinx.cinterop.toKString

actual fun getenv(name: String): String? = platform.posix.getenv(name)?.toKString()
