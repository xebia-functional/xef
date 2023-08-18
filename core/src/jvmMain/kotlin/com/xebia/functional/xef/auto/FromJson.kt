package com.xebia.functional.xef.auto

fun interface FromJson<A> {
  fun fromJson(a: String): A
}
