package com.xebia.functional.openai.infrastructure

fun <A> A.asListOfOne(): List<A> = listOf(this)
