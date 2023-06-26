package com.xebia.functional.xef.java.auto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private CoreAIScope coreAIScope;
    private ObjectMapper om;
    private JsonSchemaGenerator schemaGen;

    public AIScope(CoreAIScope coreAIScope) {
        this.coreAIScope = coreAIScope;
        this.om = new ObjectMapper();
        this.schemaGen = new JsonSchemaGenerator(om);
    }

    public AIScope(CoreAIScope coreAIScope, ObjectMapper om) {
        this.coreAIScope = coreAIScope;
        this.om = om;
        this.schemaGen = new JsonSchemaGenerator(om);
    }

    public static <T> T run(Function1<AIScope, T> block) {
        OpenAIConfig config = new OpenAIConfig();
        KtorOpenAIClient client = new KtorOpenAIClient(config);
        try {
            Embeddings embeddings = new OpenAIEmbeddings(config, client);
            VectorStore vectorStore = new LocalVectorStore(embeddings);
            CoreAIScope scope = new CoreAIScope(LLMModel.getGPT_3_5_TURBO(), LLMModel.getGPT_3_5_TURBO_FUNCTIONS(), client, vectorStore, embeddings, 3, "user", false, 0.4, 1, 20, 500);
            return block.invoke(new AIScope(scope));
        } finally {
            client.close();
        }
    }

    private <T> T undefined() {
        throw new RuntimeException("Method is undefined");
    }

    public <A> A prompt(String prompt, Class<A> cls) {
        return prompt(prompt, cls, coreAIScope.getMaxDeserializationAttempts(), coreAIScope.getDefaultModel(), coreAIScope.getUser(), coreAIScope.getEcho(), coreAIScope.getNumberOfPredictions(), coreAIScope.getTemperature(), coreAIScope.getDocsInContext(), coreAIScope.getMinResponseTokens());
    }

    public <A> A prompt(String prompt, Class<A> cls, Integer maxAttempts, LLMModel llmModel, String user, Boolean echo, Integer n, Double temperature, Integer bringFromContext, Integer minResponseTokens) {
        ObjectMapper om = new ObjectMapper();

        Function1<? super String, ? extends A> decoder = (json) -> {
            try {
                return om.readValue(json, cls);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };

        String schema;
        try {
            schema = om.writeValueAsString(schemaGen.generateSchema(cls));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<CFunction> functions = Collections.singletonList(
                new CFunction(cls.getSimpleName(), "Generated function for " + cls.getSimpleName(), schema)
        );

        try {
            return LoomAdapter.apply((continuation) -> coreAIScope.<A>promptWithSerializer(prompt, functions, decoder, maxAttempts, llmModel, user, echo, n, temperature, bringFromContext, minResponseTokens, continuation));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> promptMessage(String prompt, LLMModel llmModel, List<CFunction> functions, String user, Boolean echo, Integer n, Double temperature, Integer bringFromContext, Integer minResponseTokens) {
        try {
            return LoomAdapter.apply((continuation) -> coreAIScope.promptMessage(prompt, llmModel, functions, user, echo, n, temperature, bringFromContext, minResponseTokens, continuation));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T contextScope(List<String> docs) {
        try {
            return LoomAdapter.apply(continuation -> coreAIScope.contextScopeWithDocs(docs, undefined(), continuation));
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
            ImagesGenerationResponse response = LoomAdapter.apply(continuation -> coreAIScope.images(prompt, user, n, size, bringFromContext, continuation));

            return CollectionsKt.map(response.getData(), ImageGenerationUrl::getUrl);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void close() {
        coreAIScope.close();
    }
}
