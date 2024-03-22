package com.xebia.functional.xef.prompt.lang

import ai.xef.openai.StandardModel
import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.prompt
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * Infer is a DSL for generating structured output from structured input. Infer allows the input
 * values to be inferred with a minimal DSL which is then replaced with the actual values inside the
 * LLM thought process. Assumes a LLM with Sudolang like capabilities or understanding of structured
 * input.
 */
class Infer(
  val requestModel: CreateChatCompletionRequestModel,
  val model: ChatApi,
  val conversation: Conversation,
) {

  class Replacement<out A>(val name: String, val value: A, val config: Config)

  class Config(val modifiers: List<Pair<String, String>>) {
    companion object {
      operator fun invoke(vararg modifiers: Pair<String, String>): Config =
        Config(modifiers.toList())
    }
  }

  class Scope {

    val replacements: MutableList<Replacement<*>> = mutableListOf()

    val inferInt: Int
      get() = inferInt()

    val inferFloat: Float
      get() = inferFloat()

    val inferDouble: Double
      get() = inferDouble()

    val inferString: String
      get() = inferString()

    val placeholder
      get() = "${'$'}generate${'$'}"

    fun inferInt(config: Config = Config()): Int = infer(placeholder, Int.MAX_VALUE, config)

    fun inferFloat(config: Config = Config()): Float = infer(placeholder, Float.MAX_VALUE, config)

    fun inferDouble(config: Config = Config()): Double =
      infer(placeholder, Double.MAX_VALUE, config)

    fun inferString(config: Config = Config()): String = infer(placeholder, placeholder, config)

    private fun <A> infer(name: String, value: A, config: Config): A {
      replacements.add(Replacement(name, value, config))
      return value
    }
  }

  suspend inline operator fun <reified A, reified B> invoke(
    prompt: Prompt<CreateChatCompletionRequestModel>,
    block: Scope.() -> A
  ): B {
    val scope = Scope()
    val request = block(scope)
    var input = Json.encodeToString(serializer<A>(), request)
    scope.replacements.forEach { replacement ->
      input = replaceInferPlaceholder(input, replacement)
    }
    return model.prompt(
      prompt =
        Prompt(StandardModel(requestModel)) {
          +prompt
          +system("Stay in role and follow the directives of the function `Process`")
          +system(
            """
              Process(input) {
                 STOP, Carefully consider all instructions in this function
                 STOP, first replace all placeholders ${scope.placeholder} in input 
                 Consider in your output ALL properties in the input that are not placeholders
                 Reflect their information in your output
                 target 
                  |> critique 
                  |> fix(critique) 
                  |> applyCritique(target)
                 produce output in valid json
              }
            """
              .trimIndent()
          )
          +user("Process($input)")
        },
      scope = conversation,
      serializer = serializer<B>()
    )
  }

  fun replaceInferPlaceholder(input: String, replacement: Replacement<*>): String =
    input.replaceFirst(
      oldValue = replacement.value.toString(),
      newValue =
        if (replacement.config.modifiers.isEmpty()) {
          replacement.name
        } else {
          replacement.name +
            ":" +
            replacement.config.modifiers.joinToString(",") { (k, v) -> "$k:$v" } +
            ""
        }
    )

  companion object {}
}
