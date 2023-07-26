package com.xebia.functional.xef.java.auto;

import com.xebia.functional.xef.auto.ExecutionContext;
import com.xebia.functional.xef.auto.JVMCoreAIScope;
import com.xebia.functional.xef.auto.PromptConfiguration;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;
import com.xebia.functional.xef.auto.llm.openai.OpenAIEmbeddings;
import com.xebia.functional.xef.embeddings.Embeddings;
import com.xebia.functional.xef.llm.ChatWithFunctions;
import com.xebia.functional.xef.vectorstores.ConversationId;
import com.xebia.functional.xef.vectorstores.VectorStore;

import java.util.concurrent.CompletableFuture;

public class AIScope extends JVMCoreAIScope {

    public AIScope() {
        super(new OpenAIEmbeddings(OpenAI.DEFAULT_EMBEDDING));
    }

    public AIScope(Embeddings embeddings) {
        super(embeddings);
    }

    public AIScope(Embeddings embeddings, VectorStore context) {
        super(embeddings, context);
    }

    public AIScope(Embeddings embeddings, VectorStore context, ExecutionContext executionContext) {
        super(embeddings, context, executionContext);
    }

    public AIScope(Embeddings embeddings, VectorStore context, ExecutionContext executionContext, ConversationId conversationId) {
        super(embeddings, context, executionContext, conversationId);
    }

    private final ChatWithFunctions defaultModel = OpenAI.DEFAULT_SERIALIZATION;

    public <A> CompletableFuture<A> prompt(
            String prompt,
            Class<A> target
    ) {
        return promptAsync(defaultModel, prompt, target, PromptConfiguration.DEFAULTS);
    }

    public <A> CompletableFuture<A> prompt(
            String prompt,
            Class<A> target,
            PromptConfiguration promptConfiguration
    ) {
        return promptAsync(defaultModel, prompt, target, promptConfiguration);
    }

}
