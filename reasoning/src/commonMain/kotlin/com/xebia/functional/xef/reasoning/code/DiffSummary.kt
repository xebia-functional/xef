package com.xebia.functional.xef.reasoning.code

import com.xebia.functional.xef.conversation.AutoClose
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.autoClose
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.reasoning.text.summarize.Summarize
import com.xebia.functional.xef.reasoning.text.summarize.SummaryLength
import com.xebia.functional.xef.reasoning.tools.Tool
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class DiffSummary(
  private val serialization: ChatWithFunctions,
  private val chat: Chat,
  private val scope: Conversation
) : Tool, AutoClose by autoClose(), AutoCloseable {

  private val client = HttpClient {}

  private val summarize =




    Summarize(model = chat, scope = scope, summaryLength = SummaryLength.Bound(3000))

  private val logger: KLogger = KotlinLogging.logger {}

  override val name: String = "DiffSummary"
  override val description: String =
    "Summarize code diffs given a diff url and creates a PR description."

  @Serializable data class ExtractedUrl(val url: String)

  override suspend fun invoke(input: String): String {
    val extracted: ExtractedUrl =
      serialization.prompt(
        prompt =
          Prompt(
            """|
        |Please provide the url that you want to read a diff from
        |given this input:
        |
        |$input
      """
              .trimMargin()
          ),
        scope = scope,
        serializer = ExtractedUrl.serializer()
      )

    logger.info { "Reading url ${extracted.url}" }
    val diff =
      client.request {
        url(extracted.url)
        method = HttpMethod.Get
      }
    return if (diff.status != HttpStatusCode.OK) {
      "Could not read diff from url ${extracted.url}"
    } else {
      val content = diff.bodyAsText()
      if (chat.modelType.encoding.countTokens(content) >= chat.modelType.maxContextLength / 2) {
        val summary = summarize(diff.bodyAsText())
        createPRDescription(summary)
      } else {
        createPRDescription(content)
      }
    }
  }

  private suspend fun createPRDescription(summary: String): String =
    chat.promptMessage(
      prompt =
        Prompt {
          +system("Create Pull Request Description")
          +assistant(
            "I will roleplay as an expert software engineer implementing a service to read a .diff file from a URL and create a Pull Request description with an automatically inferred user intent."
          )
          +assistant(
            "I will use the following program to return a Pull Request description to the user:"
          )
          +systemPrompt()
          +user("Set Summary = $summary")
          +user("CreatePRDescription()")
          +assistant(
            "A great, concise and neutral toned Pull Request description for this summary is:"
          )
        },
      scope = scope
    )

  override fun close() {
    client.close()
  }

  companion object {
    fun systemPrompt(): Prompt = Prompt {
      +system(
        // language=yaml
        """
            # CreatePRDescription  
            PRDescriptionCreator:
              Roleplay: "expert software engineer implementing a service to read a .diff file from a URL and create a PR description with an automatically inferred user intent."
              DevProcess:
                State:
                  Summary: "String"
                  Content: "String"
                InferUserIntent:
                  Description: "Analyze the content to infer the user's intent for creating this PR. Must clearly articulate the reason, context, and goal."
                CreatePRDescription:
                  Description: "Construct a PR description based on the inferred user's intent and extracted content. The description must: - Clearly articulate the inferred user's intent for creating this PR. - Provide a concise summary of the content. - Be clear, concise, and informative."
                  Style guide:
                    Favor: "clear, understandable code."
                    Handle: "potential errors like invalid content, intent inference issues, etc."
                    Description: 
                      Validate and read the Summary for the .dff content
                      InferUserIntent() 
                      CreatePRDescription() 
                      Return the PR description
              Instructions: "When asked to implement this functionality, please carefully follow the instructions above, ensuring that the user's intent is automatically inferred and added to the PR description. 🙏"
          """
          .trimIndent()
      )
    }
  }
}
