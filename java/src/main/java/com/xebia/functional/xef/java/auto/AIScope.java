package com.xebia.functional.xef.java.auto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationModule;
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationOption;
import com.xebia.functional.xef.agents.Search;
import com.xebia.functional.xef.auto.CoreAIScope;
import com.xebia.functional.xef.auto.PromptConfiguration;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;
import com.xebia.functional.xef.auto.llm.openai.OpenAIEmbeddings;
import com.xebia.functional.xef.embeddings.Embeddings;
import com.xebia.functional.xef.llm.Chat;
import com.xebia.functional.xef.llm.ChatWithFunctions;
import com.xebia.functional.xef.llm.Images;
import com.xebia.functional.xef.llm.models.functions.CFunction;
import com.xebia.functional.xef.llm.models.images.ImageGenerationUrl;
import com.xebia.functional.xef.llm.models.images.ImagesGenerationResponse;
import com.xebia.functional.xef.pdf.Loader;
import com.xebia.functional.xef.sql.SQL;
import com.xebia.functional.xef.textsplitters.TextSplitter;
import com.xebia.functional.xef.vectorstores.LocalVectorStore;
import com.xebia.functional.xef.vectorstores.VectorStore;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import kotlin.collections.CollectionsKt;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function1;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.ExecutorsKt;
import kotlinx.coroutines.JobKt;
import kotlinx.coroutines.future.FutureKt;
import org.jetbrains.annotations.NotNull;

public class AIScope implements AutoCloseable {
    private final CoreAIScope scope;
    private final ObjectMapper om;
    private final SchemaGenerator schemaGenerator;
    private final ExecutorService executorService;
    private final CoroutineScope coroutineScope;

    public AIScope(ObjectMapper om, Embeddings embeddings, ExecutorService executorService) {
        this.om = om;
        this.executorService = executorService;
        this.coroutineScope = () -> ExecutorsKt.from(executorService).plus(JobKt.Job(null));
        JakartaValidationModule module = new JakartaValidationModule(
                JakartaValidationOption.NOT_NULLABLE_FIELD_IS_REQUIRED,
                JakartaValidationOption.INCLUDE_PATTERN_EXPRESSIONS
        );
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_7, OptionPreset.PLAIN_JSON)
                .with(module);
        SchemaGeneratorConfig config = configBuilder.build();
        this.schemaGenerator = new SchemaGenerator(config);
        VectorStore vectorStore = new LocalVectorStore(embeddings);
        this.scope = new CoreAIScope(embeddings, vectorStore);
    }

    public AIScope(Embeddings embeddings, ExecutorService executorService) {
        this(new ObjectMapper(), embeddings, executorService);
    }


    public AIScope(ExecutorService executorService) {
        this(new ObjectMapper(), new OpenAIEmbeddings(OpenAI.DEFAULT_EMBEDDING), executorService);
    }

    public AIScope() {
        this(new ObjectMapper(), new OpenAIEmbeddings(OpenAI.DEFAULT_EMBEDDING), Executors.newCachedThreadPool(new AIScopeThreadFactory()));
    }

    private AIScope(CoreAIScope nested, AIScope outer) {
        this.om = outer.om;
        this.executorService = outer.executorService;
        this.coroutineScope = outer.coroutineScope;
        this.schemaGenerator = outer.schemaGenerator;
        this.scope = nested;
    }

    public <A> CompletableFuture<A> prompt(String prompt, Class<A> cls) {
        return prompt(prompt, cls, OpenAI.DEFAULT_SERIALIZATION, PromptConfiguration.DEFAULTS);
    }

    public <A> CompletableFuture<A> prompt(String prompt, Class<A> cls, ChatWithFunctions llmModel, PromptConfiguration promptConfiguration) {
        Function1<? super String, ? extends A> decoder = json -> {
            try {
                return om.readValue(json, cls);
            } catch (JsonProcessingException e) {
                // TODO AIError ex = new AIError.JsonParsing(json, maxAttempts, e);
                throw new RuntimeException(e);
            }
        };

        String schema = schemaGenerator.generateSchema(cls).toString();

        List<CFunction> functions = Collections.singletonList(
                new CFunction(cls.getSimpleName(), "Generated function for " + cls.getSimpleName(), schema)
        );

        return future(continuation -> scope.promptWithSerializer(llmModel, prompt, functions, decoder, promptConfiguration, continuation));
    }

    public CompletableFuture<String> promptMessage(String prompt) {
        return promptMessage(OpenAI.DEFAULT_CHAT, prompt, PromptConfiguration.DEFAULTS);
    }

    public CompletableFuture<String> promptMessage(Chat llmModel, String prompt, PromptConfiguration promptConfiguration) {
        return future(continuation -> scope.promptMessage(llmModel, prompt, promptConfiguration, continuation));
    }

    public CompletableFuture<List<String>> promptMessages(Chat llmModel, String prompt, List<CFunction> functions, PromptConfiguration promptConfiguration) {
        return future(continuation -> scope.promptMessages(llmModel, prompt, functions, promptConfiguration, continuation));
    }

    public <A> CompletableFuture<A> contextScope(Function1<Embeddings, VectorStore> store, Function1<AIScope, CompletableFuture<A>> f) {
        return future(continuation -> scope.contextScope(store.invoke(scope.getEmbeddings()), (coreAIScope, continuation1) -> {
            AIScope nestedScope = new AIScope(coreAIScope, AIScope.this);
            return FutureKt.await(f.invoke(nestedScope), continuation);
        }, continuation));
    }


    public <A> CompletableFuture<A> contextScope(VectorStore store, Function1<AIScope, CompletableFuture<A>> f) {
        return future(continuation -> scope.contextScope(store, (coreAIScope, continuation1) -> {
            AIScope nestedScope = new AIScope(coreAIScope, AIScope.this);
            return FutureKt.await(f.invoke(nestedScope), continuation);
        }, continuation));
    }

    public <A> CompletableFuture<A> contextScope(CompletableFuture<List<String>> docs, Function1<AIScope, CompletableFuture<A>> f) {
        return docs.thenCompose(d ->
                future(continuation -> scope.contextScopeWithDocs(d, (coreAIScope, continuation1) -> {
                    AIScope nestedScope = new AIScope(coreAIScope, AIScope.this);
                    return FutureKt.await(f.invoke(nestedScope), continuation);
                }, continuation)));
    }

    public <A> CompletableFuture<A> contextScope(List<String> docs, Function1<AIScope, CompletableFuture<A>> f) {
        return future(continuation -> scope.contextScopeWithDocs(docs, (coreAIScope, continuation1) -> {
            AIScope nestedScope = new AIScope(coreAIScope, AIScope.this);
            return FutureKt.await(f.invoke(nestedScope), continuation);
        }, continuation));
    }

    public CompletableFuture<List<String>> pdf(String url, TextSplitter splitter) {
        return future(continuation -> Loader.pdf(url, splitter, continuation));
    }

    public CompletableFuture<List<String>> pdf(File file, TextSplitter splitter) {
        return future(continuation -> Loader.pdf(file, splitter, continuation));
    }

    public CompletableFuture<List<String>> images(Images model, String prompt, Integer numberOfImages, String size, PromptConfiguration promptConfiguration) {
        return this.<ImagesGenerationResponse>future(continuation -> scope.images(model, prompt, numberOfImages, size, promptConfiguration, continuation))
                .thenApply(response -> CollectionsKt.map(response.getData(), ImageGenerationUrl::getUrl));
    }

    public CompletableFuture<List<String>> search(String prompt) {
        return future(continuation -> Search.search(prompt, continuation));
    }

    public CompletableFuture<String> getInterestingPromptsForDatabase(SQL sql) {
        return future(continuation -> sql.getInterestingPromptsForDatabase(scope, continuation));
    }

    private <A> CompletableFuture<A> future(Function1<? super Continuation<? super A>, ? extends Object> block) {
        return FutureKt.future(
                coroutineScope,
                coroutineScope.getCoroutineContext(),
                CoroutineStart.DEFAULT,
                (coroutineScope, continuation) -> block.invoke(continuation)
        );
    }

    @Override
    public void close() {
        CoroutineScopeKt.cancel(coroutineScope, null);
        executorService.shutdown();
    }

    private static class AIScopeThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread t = new Thread(r);
            t.setName("xef-ai-scope-worker-" + counter.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }
}
