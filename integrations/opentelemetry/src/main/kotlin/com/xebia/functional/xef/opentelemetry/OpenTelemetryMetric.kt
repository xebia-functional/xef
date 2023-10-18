package com.xebia.functional.xef.opentelemetry

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.metrics.Metric
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.store.ConversationId
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context

class OpenTelemetryMetric(
    private val config: OpenTelemetryConfig = OpenTelemetryConfig.DEFAULT
) : Metric {

    private val conversations = mutableListOf<Pair<ConversationId, Context>>()

    private val openTelemetry = config.newInstance()

    override suspend fun <A> promptSpan(conversation: Conversation, prompt: Prompt, block: suspend Metric.() -> A): A {
        val cid = conversation.conversationId ?: return block()

        val parentContext = cid.getParentConversation()

        val span = getTracer()
            .spanBuilder("Prompt: ${prompt.messages.lastOrNull()?.content ?: "empty"}")
            .setParent(parentContext)
            .startSpan()

        return try {
            val output = block()
            span.makeCurrent().use {
                span.setAttribute("number-of-messages", prompt.messages.count().toString())
                span.setAttribute("last-message", prompt.messages.lastOrNull()?.content ?: "empty")
            }
            output
        } finally {
            span.end()
        }
    }

    override fun log(conversation: Conversation, message: String) {
        val cid = conversation.conversationId ?: return

        val parentContext = cid.getParentConversation()

        val span: Span = getTracer().spanBuilder(message)
            .setParent(parentContext)
            .startSpan()
        span.end()
    }

    private fun ConversationId.getParentConversation(): Context {
        val parent = conversations.find { it.first == this }?.second
        return if (parent == null) {
            val newParent = getTracer()
                .spanBuilder(value)
                .startSpan()
            newParent.end()
            val newContext = Context.current().with(newParent)
            conversations.add(this to newContext)
            newContext
        } else parent
    }

    private fun getTracer(scopeName: String? = null): Tracer =
        openTelemetry.getTracer(scopeName ?: config.defaultScopeName)

}
