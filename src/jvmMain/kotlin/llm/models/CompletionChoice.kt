package llm.models

import arrow.core.raise.nullable
import com.theokanning.openai.completion.CompletionChoice as JCompletionChoice

fun CompletionChoice.Companion.fromJava(j: JCompletionChoice): CompletionChoice? = nullable {
  val text = j.text.bind()
  val index = j.index.bind()
  val finishReason = j.finish_reason.bind()
  CompletionChoice(text, index, finishReason)
}
