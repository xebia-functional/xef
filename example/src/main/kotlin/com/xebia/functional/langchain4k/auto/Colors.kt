package com.xebia.functional.langchain4k.auto

import com.xebia.functional.auto.ai
import com.xebia.functional.auto.getOrElse
import kotlinx.serialization.Serializable

@Serializable
data class Colors(val colors: List<String>)

suspend fun main() {
    ai {
        val colors: Colors = prompt("a selection of 10 beautiful colors that go well together")
        println(colors)
    }.getOrElse { println(it) }
}
