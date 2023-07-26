package com.xebia.functional.xef.auto

import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.vectorstores.ConversationId
import com.xebia.functional.xef.vectorstores.LocalVectorStore
import com.xebia.functional.xef.vectorstores.VectorStore
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import java.util.concurrent.CompletableFuture

/**
 * The [CoreAIScope] is the context in which [AI] values are run. It encapsulates all the
 * dependencies required to run [AI] values, and provides convenient syntax for writing [AI] based
 * programs.
 */
open class JVMCoreAIScope
@JvmOverloads
constructor(
  override val embeddings: Embeddings,
  override val context: VectorStore = LocalVectorStore(embeddings),
  private val executionContext: ExecutionContext = ExecutionContext(),
  override val conversationId: ConversationId? = ConversationId(UUID.generateUUID().toString())
) : CoreAIScope, AutoCloseable, AutoClose by autoClose() {

  @JvmOverloads
  fun <A> promptAsync(
    chatWithFunctions: ChatWithFunctions,
    prompt: String,
    target: Class<A>,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
  ): CompletableFuture<A> =
    executionContext.future { chatWithFunctions.prompt(Prompt(prompt), target, promptConfiguration) }

  suspend fun <A> ChatWithFunctions.prompt(
    prompt: Prompt,
    target: Class<A>,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
  ): A {
    val serializerName = target.simpleName
    val jsonSchema = encodeJsonSchema(target)
    return prompt(
      prompt = prompt,
      context = context,
      serializerName = serializerName,
      jsonSchema = jsonSchema,
      conversationId = conversationId,
      serializer = { json -> serialize(target, json) },
      promptConfiguration = promptConfiguration
    )
  }

  fun <A> contextScopeAsync(docs: List<String>, block: (CoreAIScope) -> CompletableFuture<A>): CompletableFuture<A> =
    executionContext.future {
      contextScope(docs) { block(this). }
    }
}
