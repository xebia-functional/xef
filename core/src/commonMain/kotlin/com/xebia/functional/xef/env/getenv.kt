package com.xebia.functional.xef.env

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
