package com.xebia.functional.xef.env

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString

@OptIn(ExperimentalForeignApi::class)
actual fun getenv(name: String): String? = platform.posix.getenv(name)?.toKString()
