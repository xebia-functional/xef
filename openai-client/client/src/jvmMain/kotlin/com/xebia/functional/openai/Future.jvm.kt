package com.xebia.functional.openai

import java.util.concurrent.CompletableFuture as JavaFuture

actual typealias CompletableFuture<T> = JavaFuture<T>

