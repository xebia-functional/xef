package com.xebia.functional.xef.conversation.conversations

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.promptMessage
import com.xebia.functional.xef.prompt.Prompt

suspend fun main() {
  OpenAI.conversation {
    val emailMessage =
      Prompt(
        """
                |You are a Marketing Responsible and have the information about different products. You have to prepare 
                |an email template with the personal information
            """
          .trimMargin()
      )

    val email: String = promptMessage(emailMessage)

    println("Prompt:\n $emailMessage")
    println("Response:\n $email")

    val summarizePrompt =
      Prompt(
        """
                |You are a Marketing Responsible and have the information about the best rated products. 
                |Summarize the next information: 
                |Love this product and so does my husband! He tried it because his face gets chapped and red from 
                |working outside. It actually helped by about 60%! I love it cuz it's lightweight and smells so yummy! 
                |After applying makeup, it doesn't leave streaks like other moisturizers cause. i would definitely use 
                |this!
                |
                |I've been using this for 10+yrs now. I don't have any noticeable 
                |wrinkles at all. I use Estée Lauder's micro essence then advance repair serum before I apply this 
                |lotion. A little goes a long way! It does feel greasier than most face lotions that I've tried 
                |previously but I don't apply much. I enjoy cucumber like scent of the face lotion. I have combination 
                |skin and never broke out using this. This is my daily skincare product with or without makeup. And it 
                |has SPF but I also apply Kravebeauty SPF on top as well for extra protection
            """
          .trimMargin()
      )

    val summarize: String = promptMessage(summarizePrompt)

    println("Prompt:\n $summarizePrompt}")
    println("Response:\n $summarize")

    val meaningPrompt =
      Prompt(
        """
                |What is the meaning of life?
            """
          .trimMargin()
      )

    val meaning: String = promptMessage(meaningPrompt)

    println("Prompt:\n $meaningPrompt}")
    println("Response:\n $meaning")
  }
}
