package com.xebia.functional.xef.java.auto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.xebia.functional.xef.auto.CoreAIScope;
import com.xebia.functional.xef.embeddings.Embeddings;
import com.xebia.functional.xef.embeddings.OpenAIEmbeddings;
import com.xebia.functional.xef.env.OpenAIConfig;
import com.xebia.functional.xef.llm.openai.KtorOpenAIClient;
import com.xebia.functional.xef.llm.openai.LLMModel;
import com.xebia.functional.xef.llm.openai.CFunction;
import com.xebia.functional.xef.llm.openai.images.ImageGenerationUrl;
import com.xebia.functional.xef.llm.openai.images.ImagesGenerationResponse;
import com.xebia.functional.xef.textsplitters.TextSplitter;
import com.xebia.functional.xef.vectorstores.LocalVectorStore;
import com.xebia.functional.xef.vectorstores.VectorStore;
import kotlin.collections.CollectionsKt;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function1;
import com.xebia.functional.xef.pdf.PDFLoaderKt;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.ExecutorsKt;
import kotlinx.coroutines.JobKt;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.future.FutureKt;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class AIScope implements AutoCloseable {
    private final CoreAIScope scope;
    private final ObjectMapper om;
    private final JsonSchemaGenerator schemaGen;
    private final KtorOpenAIClient client;
    private final ExecutorService executorService;
    private final CoroutineScope coroutineScope;

    public AIScope(ObjectMapper om, OpenAIConfig config, ExecutorService executorService) {
        this.om = om;
        this.executorService = executorService;
        this.coroutineScope = () -> ExecutorsKt.from(executorService).plus(JobKt.Job(null));
        this.schemaGen = new JsonSchemaGenerator(om);
        this.client = new KtorOpenAIClient(config);
        Embeddings embeddings = new OpenAIEmbeddings(config, client);
        VectorStore vectorStore = new LocalVectorStore(embeddings);
        this.scope = new CoreAIScope(LLMModel.getGPT_3_5_TURBO(), LLMModel.getGPT_3_5_TURBO_FUNCTIONS(), client, vectorStore, embeddings, 3, "user", false, 0.4, 1, 20, 500);
    }

    public AIScope(OpenAIConfig config) {
        this(new ObjectMapper(), config, Executors.newCachedThreadPool(new AIScopeThreadFactory()));
    }

    public AIScope(OpenAIConfig config, ExecutorService executorService) {
        this(new ObjectMapper(), config, executorService);
    }

    public AIScope() {
        this(new ObjectMapper(), new OpenAIConfig(), Executors.newCachedThreadPool(new AIScopeThreadFactory()));
    }

    private AIScope(CoreAIScope nested, AIScope outer) {
        this.om = outer.om;
        this.executorService = outer.executorService;
        this.coroutineScope = outer.coroutineScope;
        this.schemaGen = outer.schemaGen;
        this.client = outer.client;
        this.scope = nested;
    }

    public <A> CompletableFuture<A> prompt(String prompt, Class<A> cls) {
        return prompt(prompt, cls, scope.getMaxDeserializationAttempts(), scope.getDefaultSerializationModel(), scope.getUser(), scope.getEcho(), scope.getNumberOfPredictions(), scope.getTemperature(), scope.getDocsInContext(), scope.getMinResponseTokens());
    }

    public <A> CompletableFuture<A> prompt(String prompt, Class<A> cls, Integer maxAttempts, LLMModel llmModel, String user, Boolean echo, Integer n, Double temperature, Integer bringFromContext, Integer minResponseTokens) {
        Function1<? super String, ? extends A> decoder = json -> {
            try {
                return om.readValue(json, cls);
            } catch (JsonProcessingException e) {
                // TODO AIError ex = new AIError.JsonParsing(json, maxAttempts, e);
                throw new RuntimeException(e);
            }
        };

        String schema;
        try {
            JsonSchema jsonSchema = schemaGen.generateSchema(cls);
            jsonSchema.setId(null);
            schema = om.writeValueAsString(jsonSchema);
        } catch (JsonProcessingException e) {
            // TODO AIError ex = new AIError.JsonParsing(json, maxAttempts, e);
            throw new RuntimeException(e);
        }

        List<CFunction> functions = Collections.singletonList(
                new CFunction(cls.getSimpleName(), "Generated function for " + cls.getSimpleName(), schema)
        );

        return future(continuation -> scope.promptWithSerializer(prompt, functions, decoder, maxAttempts, llmModel, user, echo, n, temperature, bringFromContext, minResponseTokens, continuation));
    }

    public CompletableFuture<List<String>> promptMessage(String prompt, LLMModel llmModel, List<CFunction> functions, String user, Boolean echo, Integer n, Double temperature, Integer bringFromContext, Integer minResponseTokens) {
        return future(continuation -> scope.promptMessage(prompt, llmModel, functions, user, echo, n, temperature, bringFromContext, minResponseTokens, continuation));
    }

    public <A> CompletableFuture<A> contextScope(List<String> docs, Function1<AIScope, CompletableFuture<A>> f) {
        return future(continuation -> scope.contextScopeWithDocs(docs, (coreAIScope, continuation1) -> {
            AIScope nestedScope = new AIScope(coreAIScope, AIScope.this);
            return FutureKt.await(f.invoke(nestedScope), continuation);
        }, continuation));
    }

    public CompletableFuture<List<String>> pdf(String url, TextSplitter splitter) {
        return future(continuation -> PDFLoaderKt.pdf(url, splitter, continuation));
    }

    public CompletableFuture<List<String>> pdf(File file, TextSplitter splitter) {
        return future(continuation -> PDFLoaderKt.pdf(file, splitter, continuation));
    }

    public CompletableFuture<List<String>> images(String prompt, String user, String size, Integer bringFromContext, Integer n) {
        return this.<ImagesGenerationResponse>future(continuation -> scope.images(prompt, user, n, size, bringFromContext, continuation))
                .thenApply(response -> CollectionsKt.map(response.getData(), ImageGenerationUrl::getUrl));
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
        client.close();
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
