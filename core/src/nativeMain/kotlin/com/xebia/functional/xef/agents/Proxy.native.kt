package com.xebia.functional.xef.agents

import kotlin.coroutines.Continuation
import kotlin.reflect.KClass

actual inline fun <reified A> proxy(
  interfaces: List<KClass<*>>,
  invocationHandler: (methodCall: MethodCall, continuation: Continuation<Any?>) -> Any?
): A {
  TODO("Not yet implemented")
}
