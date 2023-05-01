package com.xebia.functiona.langchain4k.auto

import com.xebia.functional.auto.ai

suspend fun main() {
    val love: List<String> = ai("tell me you like me with just emojis")
    println(love)
}
