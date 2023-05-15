package com.xebia.functional.xef.env

external val process: dynamic

/**
 * We wrap it in runCatching because this only works in NodeJS. In the browser, we get a
 * ReferenceError: "process" is not defined.
 */
actual fun getenv(name: String): String? = runCatching { process.env[name] as String? }.getOrNull()
