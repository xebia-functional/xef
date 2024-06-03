package com.xebia.functional.xef.agents

import com.xebia.functional.openai.generated.model.FunctionObject
import kotlinx.serialization.json.JsonObject
import kotlin.coroutines.Continuation
import kotlin.reflect.KClass
import kotlin.reflect.KType

data class MethodCall(
  val name: String,
  val returnType: KType,
  val arguments: List<Argument>,
  val tools : List<AvailableTool>
)

data class AvailableTool(
  val name: String,
  val description: String,
  val parameters: JsonObject
)

data class Argument(
  val name: String,
  val type: KClass<*>,
  val annotations : List<Annotation>,
  val value: Any?
)

expect inline fun <reified A> proxy(
  interfaces: List<KClass<*>> = emptyList(),
  crossinline invocationHandler: (methodCall: MethodCall, continuation: Continuation<Any?>) -> Any?
): A

