package com.xebia.functional.xef.java.auto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationModule;
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationOption;
import com.xebia.functional.xef.agents.Search;
import com.xebia.functional.xef.auto.CoreAIScope;
import com.xebia.functional.xef.auto.PromptConfiguration;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;
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
import com.xebia.functional.xef.vectorstores.VectorStore;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.future.FutureKt;

public class AIScope implements AutoCloseable {
    private final CoreAIScope scope;
    private final ObjectMapper om;
    private ExecutionContext exec;
    private final SchemaGenerator schemaGenerator;

    public AIScope(ObjectMapper om, ExecutionContext executionContext) {
        this.om = om;
        this.exec = executionContext;
        JakartaValidationModule module = new JakartaValidationModule(
                JakartaValidationOption.NOT_NULLABLE_FIELD_IS_REQUIRED,
                JakartaValidationOption.INCLUDE_PATTERN_EXPRESSIONS
        );
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_7, OptionPreset.PLAIN_JSON)
                .with(module);
        SchemaGeneratorConfig config = configBuilder.build();
        this.schemaGenerator = new SchemaGenerator(config);
        this.scope = executionContext.getCoreScope();
    }

    public ExecutionContext getExec() {
        return exec;
    }

    public AIScope(ExecutionContext executionContext) {
        this(new ObjectMapper(), executionContext);
    }

    public AIScope() {
        this(new ObjectMapper(), new ExecutionContext());
    }

    private AIScope(CoreAIScope nested, AIScope outer) {
        this.om = outer.om;
        this.schemaGenerator = outer.schemaGenerator;
        this.exec = outer.exec;
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

        return exec.future(continuation -> scope.promptWithSerializer(llmModel, prompt, functions, decoder, promptConfiguration, continuation));
    }

    public CompletableFuture<String> promptMessage(String prompt) {
        return promptMessage(OpenAI.DEFAULT_CHAT, prompt, PromptConfiguration.DEFAULTS);
    }

    public CompletableFuture<String> promptMessage(Chat llmModel, String prompt, PromptConfiguration promptConfiguration) {
        return exec.future(continuation -> scope.promptMessage(llmModel, prompt, promptConfiguration, continuation));
    }

    public CompletableFuture<List<String>> promptMessages(Chat llmModel, String prompt, List<CFunction> functions, PromptConfiguration promptConfiguration) {
        return exec.future(continuation -> scope.promptMessages(llmModel, prompt, functions, promptConfiguration, continuation));
    }

    public CompletableFuture<Flow<String>> promptStreaming(Chat gpt4all, String line, PromptConfiguration promptConfiguration) {
        return exec.future(continuation -> scope.promptStreaming(gpt4all, line, exec.getContext(), null, Collections.emptyList(), promptConfiguration, continuation));
    }

    public <A> CompletableFuture<A> contextScope(Function1<Embeddings, VectorStore> store, Function1<AIScope, CompletableFuture<A>> f) {
        return exec.future(continuation -> scope.contextScope(store.invoke(scope.getEmbeddings()), (coreAIScope, continuation1) -> {
            AIScope nestedScope = new AIScope(coreAIScope, AIScope.this);
            return FutureKt.await(f.invoke(nestedScope), continuation);
        }, continuation));
    }

    public <A> CompletableFuture<A> contextScope(VectorStore store, Function1<AIScope, CompletableFuture<A>> f) {
        return exec.future(continuation -> scope.contextScope(store, (coreAIScope, continuation1) -> {
            AIScope nestedScope = new AIScope(coreAIScope, AIScope.this);
            return FutureKt.await(f.invoke(nestedScope), continuation);
        }, continuation));
    }

    public <A> CompletableFuture<A> contextScope(CompletableFuture<List<String>> docs, Function1<AIScope, CompletableFuture<A>> f) {
        return docs.thenCompose(d ->
                exec.future(continuation -> scope.contextScopeWithDocs(d, (coreAIScope, continuation1) -> {
                    AIScope nestedScope = new AIScope(coreAIScope, AIScope.this);
                    return FutureKt.await(f.invoke(nestedScope), continuation);
                }, continuation)));
    }

    public <A> CompletableFuture<A> contextScope(List<String> docs, Function1<AIScope, CompletableFuture<A>> f) {
        return exec.future(continuation -> scope.contextScopeWithDocs(docs, (coreAIScope, continuation1) -> {
            AIScope nestedScope = new AIScope(coreAIScope, AIScope.this);
            return FutureKt.await(f.invoke(nestedScope), continuation);
        }, continuation));
    }

    public CompletableFuture<List<String>> pdf(String url, TextSplitter splitter) {
        return exec.future(continuation -> Loader.pdf(url, splitter, continuation));
    }

    public CompletableFuture<List<String>> pdf(File file, TextSplitter splitter) {
        return exec.future(continuation -> Loader.pdf(file, splitter, continuation));
    }

    public CompletableFuture<List<String>> images(Images model, String prompt, Integer numberOfImages, String size, PromptConfiguration promptConfiguration) {
        return exec.future(continuation -> scope.images(model, prompt, numberOfImages, size, promptConfiguration, continuation))
                .thenApply(response -> CollectionsKt.map(((ImagesGenerationResponse)response).getData(), ImageGenerationUrl::getUrl));
    }

    public CompletableFuture<List<String>> search(String prompt) {
        return exec.future(continuation -> Search.search(prompt, continuation));
    }

    public CompletableFuture<String> getInterestingPromptsForDatabase(SQL sql) {
        return exec.future(continuation -> sql.getInterestingPromptsForDatabase(scope, continuation));
    }

    @Override
    public void close(){
        exec.close();
    }

}
