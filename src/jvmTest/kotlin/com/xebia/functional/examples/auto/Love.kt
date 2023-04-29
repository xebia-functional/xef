package com.xebia.functional.examples.auto

import com.xebia.functional.auto.ai

suspend fun main() {
    val love: List<String> = ai("tell me you like me with just emojis")
    println(love)
}
