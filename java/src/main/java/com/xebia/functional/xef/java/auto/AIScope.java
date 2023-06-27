package com.xebia.functional.xef.java.auto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.xebia.functional.loom.LoomAdapter;
import com.xebia.functional.xef.auto.CoreAIScope;
import com.xebia.functional.xef.embeddings.Embeddings;
import com.xebia.functional.xef.embeddings.OpenAIEmbeddings;
import com.xebia.functional.xef.env.OpenAIConfig;
import com.xebia.functional.xef.llm.openai.KtorOpenAIClient;
import com.xebia.functional.xef.llm.openai.LLMModel;
import com.xebia.functional.xef.llm.openai.functions.CFunction;
import com.xebia.functional.xef.llm.openai.images.ImageGenerationUrl;
import com.xebia.functional.xef.llm.openai.images.ImagesGenerationResponse;
import com.xebia.functional.xef.textsplitters.TextSplitter;
import com.xebia.functional.xef.vectorstores.LocalVectorStore;
import com.xebia.functional.xef.vectorstores.VectorStore;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.functions.Function1;
import com.xebia.functional.xef.pdf.PDFLoaderKt;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class AIScope implements AutoCloseable {
    private final CoreAIScope scope;
    private final ObjectMapper om;
    private final JsonSchemaGenerator schemaGen;
    private final KtorOpenAIClient client;
    private final Embeddings embeddings;
    private final VectorStore vectorStore;

    public AIScope(ObjectMapper om, OpenAIConfig config) {
        this.om = om;
        this.schemaGen = new JsonSchemaGenerator(om);
        this.client = new KtorOpenAIClient(config);
        this.embeddings = new OpenAIEmbeddings(config, client);
        this.vectorStore = new LocalVectorStore(embeddings);
        this.scope = new CoreAIScope(LLMModel.getGPT_3_5_TURBO(), LLMModel.getGPT_3_5_TURBO_FUNCTIONS(), client, vectorStore, embeddings, 3, "user", false, 0.4, 1, 20, 500);
    }

    public AIScope(ObjectMapper om) {
        this(om, new OpenAIConfig());
    }

    public AIScope(OpenAIConfig config) {
        this(new ObjectMapper(), config);
    }

    public AIScope() {
        this(new ObjectMapper(), new OpenAIConfig());
    }

    private <T> T undefined() {
        throw new RuntimeException("Method is undefined");
    }

    public <A> A prompt(String prompt, Class<A> cls) {
        return prompt(prompt, cls, scope.getMaxDeserializationAttempts(), scope.getDefaultSerializationModel(), scope.getUser(), scope.getEcho(), scope.getNumberOfPredictions(), scope.getTemperature(), scope.getDocsInContext(), scope.getMinResponseTokens());
    }

    public <A> A prompt(String prompt, Class<A> cls, Integer maxAttempts, LLMModel llmModel, String user, Boolean echo, Integer n, Double temperature, Integer bringFromContext, Integer minResponseTokens) {
        Function1<? super String, ? extends A> decoder = (json) -> {
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

        try {
            return LoomAdapter.apply((continuation) -> scope.<A>promptWithSerializer(prompt, functions, decoder, maxAttempts, llmModel, user, echo, n, temperature, bringFromContext, minResponseTokens, continuation));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> promptMessage(String prompt, LLMModel llmModel, List<CFunction> functions, String user, Boolean echo, Integer n, Double temperature, Integer bringFromContext, Integer minResponseTokens) {
        try {
            return LoomAdapter.apply((continuation) -> scope.promptMessage(prompt, llmModel, functions, user, echo, n, temperature, bringFromContext, minResponseTokens, continuation));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T contextScope(List<String> docs) {
        try {
            return LoomAdapter.apply(continuation -> scope.contextScopeWithDocs(docs, undefined(), continuation));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> pdf(String url, TextSplitter splitter) {
        try {
            return LoomAdapter.apply(continuation -> PDFLoaderKt.pdf(url, splitter, continuation));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> pdf(File file, TextSplitter splitter) {
        try {
            return LoomAdapter.apply(continuation -> PDFLoaderKt.pdf(file, splitter, continuation));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> images(String prompt, String user, String size, Integer bringFromContext, Integer n) {
        try {
            ImagesGenerationResponse response = LoomAdapter.apply(continuation -> scope.images(prompt, user, n, size, bringFromContext, continuation));

            return CollectionsKt.map(response.getData(), ImageGenerationUrl::getUrl);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void close() {
        client.close();
    }
}
