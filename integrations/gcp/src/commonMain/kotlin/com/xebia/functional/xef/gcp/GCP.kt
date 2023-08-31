package com.xebia.functional.xef.gcp

import arrow.core.nonEmptyListOf
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.Provider
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.PlatformConversation
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.store.LocalVectorStore
import com.xebia.functional.xef.store.VectorStore
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

private const val GCP_TOKEN_ENV_VAR = "GCP_TOKEN"

class GCP(projectId: String, location: VertexAIRegion, token: String? = null) : Provider<GcpModel> {
    private val config = GcpConfig(
        token = token ?: tokenFromEnv(),
        projectId = projectId,
        location = location,
    )

    private fun tokenFromEnv(): String =
        getenv(GCP_TOKEN_ENV_VAR)
            ?: throw AIError.Env.GCP(nonEmptyListOf("missing $GCP_TOKEN_ENV_VAR env var"))

    val CODECHAT by lazy { GcpModel("codechat-bison@001", config) }
    val TEXT_EMBEDDING_GECKO by lazy { GcpModel("textembedding-gecko", config) }

    override val DEFAULT_CHAT = CODECHAT
    override val DEFAULT_EMBEDDING = TEXT_EMBEDDING_GECKO

    override fun supportedModels(): List<GcpModel> = listOf(
        CODECHAT,
        TEXT_EMBEDDING_GECKO
    )

    companion object {

    }
}

suspend inline fun <A> Provider<*>.conversation(
    store: VectorStore,
    noinline block: suspend Conversation.() -> A
): A = block(Conversation(store,  provider = this))

suspend inline fun <A> Provider<*>.conversation( // function can also be generic than specific to GCP, after class GcpEmbeddings and OpenAIEmbeddings is merged
    noinline block: suspend Conversation.() -> A
): A = block(Conversation(LocalVectorStore(GcpEmbeddings(DEFAULT_EMBEDDING)), provider = this))