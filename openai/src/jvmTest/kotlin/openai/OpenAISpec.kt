package openai

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.prompt
import com.xebia.functional.xef.conversation.llm.openai.promptMessage
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.user
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import openai.data.Answer
import openai.data.Question
import openai.data.TestFunctionsModel
import openai.data.TestModel

class OpenAISpec :
  StringSpec({
    """
        | When we are using a OpenAI scope conversation
        | the memories should have the correct size in the vector store
        | for the conversationId generated inside the conversation
        """ {
      OpenAI.conversation {
        val model = TestModel(modelType = ModelType.ADA, name = "fake-model")

        promptMessage(prompt = Prompt("question 1"), model = model)

        promptMessage(prompt = Prompt("question 2"), model = model)

        val memories = store.memories(conversationId!!, 10000)

        memories.size shouldBe 4
      }
    }

    """
        | When we are using a OpenAI scope conversation with functions
        | the memories should have the correct size in the vector store
        | for the conversationId generated inside the conversation
        """ {
      OpenAI.conversation {
        val question = Question("fake-question")
        val questionJsonString = Json.encodeToString(question)
        val answer = Answer("fake-answer")
        val answerJsonString = Json.encodeToString(answer)
        val question2 = "question 2"

        val message = mapOf(questionJsonString to answerJsonString, question2 to answerJsonString)

        val model =
          TestFunctionsModel(
            modelType = ModelType.GPT_3_5_TURBO_FUNCTIONS,
            name = "fake-model",
            responses = message
          )

        val response1: Answer = prompt(Prompt { +user(question) }, model)

        val response2: Answer = prompt(Prompt(question2), model)

        val memories = store.memories(conversationId!!, 10000)

        memories.size shouldBe 4
      }
    }
  })
