package com.xebia.functional.env

import arrow.core.raise.Raise
import arrow.core.raise.ensureNotNull

/**
 * Get an env variable by [name], or fallback to an _optional_ default value. In case no env value
 * is found or no default value is provided then it raises an error message.
 */
fun Raise<String>.env(name: String, default: String? = null): String =
  ensureNotNull(getenv(name) ?: default) { "\"$name\" configuration missing" }

/**
 * Get an env variable by [name] and [transform] it. If no env variable is found, it raises an error
 * message. The [transform] function can also raise a custom error message.
 */
fun <A : Any> Raise<String>.env(name: String, transform: Raise<String>.(String) -> A?): A {
  val string = getenv(name)?.let { transform(it) }
  return ensureNotNull(string) { "\"$name\" configuration found with $string" }
}

/**
 * Get an env variable by [name] and [transform] it, or fallback to a default value. The [transform]
 * function can raise a custom error message.
 */
fun <A : Any> Raise<String>.env(
  name: String,
  default: A,
  transform: Raise<String>.(String) -> A?
): A = getenv(name)?.let { transform(it) } ?: default

/**
 * A function that reads the configuration from the environment. This only works on JVM, Native and
 * NodeJS.
 *
 * In the browser, we default to `null` so either rely on the default values, or provide construct
 * the values explicitly.
 *
 * We might be able to support browser through webpack.
 */
expect fun getenv(name: String): String?
