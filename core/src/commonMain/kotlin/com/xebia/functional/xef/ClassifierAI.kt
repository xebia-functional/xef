package com.xebia.functional.xef

import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation

interface ClassifierAI<E> : EnumAI<E> where E : Enum<E>, E : ClassifierAI.PromptClassifier {

  interface PromptClassifier {
    fun template(input: String, output: String, context: String): String
  }

  /**
   * Classify a prompt using a given enum.
   *
   * @param input The input to the model.
   * @param output The output to the model.
   * @param context The context to the model.
   * @param model The model to use.
   * @param target The target type to return.
   * @param api The chat API to use.
   * @param conversation The conversation to use.
   * @return The classified enum.
   * @throws IllegalArgumentException If no enum values are found.
   */
  @AiDsl
  suspend fun classify(
    input: String,
    output: String,
    context: String,
    conversation: Conversation = Conversation()
  ): E {
    return invoke(
      prompt = enum.template(input, output, context),
      conversation = conversation
    )
  }


}
