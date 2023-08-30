package com.xebia.functional.xef.conversation

fun interface FromJson<A> {
  fun fromJson(a: String): A
}
