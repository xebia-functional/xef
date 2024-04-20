package com.xebia.functional.openai

expect class CompletableFuture<T> {
    fun get(): T
}
