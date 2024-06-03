package com.xebia.functional.xef.agents

import com.xebia.functional.xef.llm.chatFunction
import com.xebia.functional.xef.llm.models.functions.buildJsonSchema
import kotlinx.serialization.serializer
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.valueParameters


actual inline fun <reified A> proxy(
  interfaces: List<KClass<*>>,
  crossinline invocationHandler: (methodCall: MethodCall, continuation: Continuation<Any?>) -> Any?
): A {
  val members = A::class.members.toList()
  val systemToolsFunctionsAndAnnotations = members.filter { it.annotations.any { it is Tool } }
    .associate { it to it.annotations.filterIsInstance<Tool>().firstOrNull() }
  val availableTools = systemToolsFunctionsAndAnnotations.map { (function, tools) ->
    val toolAnnotationName = tools?.name ?: ""
    val toolName = if (toolAnnotationName != "") toolAnnotationName else function.name
    if (function.valueParameters.size != 1) error("Only one parameter is allowed for a tool function")
    val parameter = function.valueParameters.first()
    val parameterTypeDescriptor = serializer(parameter.type).descriptor
    val functionObject = buildJsonSchema(parameterTypeDescriptor)
    AvailableTool(
      name = toolName,
      description = tools?.description ?: "",
      parameters = functionObject
    )
  }
  // create a JVM dynamic proxy
  val handler = InvocationHandler { _, method, args ->
    val lastArg = args?.lastOrNull()
    val cont = lastArg as? Continuation<Any?> ?: error("Only suspend functions are supported")
    val argsButLast = args.take(args.size - 1)
    val callable = getKotlinCallable(method, members)
    val arguments = callable?.let { arguments(argsButLast, it) } ?: error("Method not found: $method")
    invocationHandler(MethodCall(
      name = callable.name,
      returnType = callable.returnType,
      arguments = arguments,
      tools = availableTools
    ), cont)
    COROUTINE_SUSPENDED
  }
  val allInterfaces = listOf(A::class.java) + interfaces.map { it.java }
  return Proxy.newProxyInstance(
    A::class.java.classLoader,
    allInterfaces.toTypedArray(),
    handler
  ) as A
}

@PublishedApi
internal fun getKotlinCallable(method: java.lang.reflect.Method, members: List<KCallable<*>>): KCallable<*>? =
  members.firstOrNull { it.name == method.name }

@PublishedApi
internal fun arguments(
  argsButLast: List<Any>,
  method: KCallable<*>
) = argsButLast.mapIndexed { index, arg ->
  Argument( //first argument is the receiver
    name = method.parameters[index + 1].name ?: "",
    type = arg.javaClass.kotlin,
    annotations = method.parameters[index + 1].annotations.toList(),
    value = arg
  )
}
