package com.xebia.functional.openai

import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

interface Defaults {

}

fun interface StreamCallback<T> : Defaults {

    fun onItem(item: T)

    fun onStart() {}

    fun onComplete(throwable: Throwable?) {}

    companion object {
        @JvmStatic
        fun <T> create(block: (T) -> Unit): StreamCallback<T> =
            StreamCallback { item -> block(item) }
    }

}
