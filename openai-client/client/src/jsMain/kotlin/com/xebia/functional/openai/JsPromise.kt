package com.xebia.functional.openai

import kotlin.js.Promise

class JsPromise<T>(executor: (resolve: (T) -> Unit, reject: (Throwable) -> Unit) -> Unit) : Promise<T>(executor) {
  fun get(): T = error("Unsupported operation")
}
