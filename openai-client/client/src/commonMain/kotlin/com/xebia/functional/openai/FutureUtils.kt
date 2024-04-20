package com.xebia.functional.openai

import kotlinx.coroutines.CoroutineScope

expect fun <T> CoroutineScope.future(block: suspend () -> T): CompletableFuture<T>

