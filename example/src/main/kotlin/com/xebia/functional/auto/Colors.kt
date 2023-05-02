package com.xebia.functional.auto

import kotlinx.serialization.Serializable

@Serializable
data class Colors(val colors: List<String>)

suspend fun main() {
    val colors: Colors = ai("a selection of 10 beautiful colors that go well together")
    println(colors)
}
