package com.xebia.functional.xef.java.auto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xebia.functional.loom.LoomAdapter;
import com.xebia.functional.xef.auto.CoreAIScope;
import com.xebia.functional.xef.llm.openai.LLMModel;
import com.xebia.functional.xef.llm.openai.functions.CFunction;
import com.xebia.functional.xef.llm.openai.images.ImageGenerationUrl;
import com.xebia.functional.xef.llm.openai.images.ImagesGenerationResponse;
import com.xebia.functional.xef.textsplitters.TextSplitter;
import kotlin.jvm.functions.Function1;
import com.xebia.functional.xef.pdf.PDFLoaderKt;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class AIScope implements AutoCloseable {
    private CoreAIScope coreAIScope;

/*    public <T> AIScope() {
        AIRuntime<T> runtime = AIRuntime.openAI();
        Function2<CoreAIScope, Continuation<T>, T> block = undefined();
        Function2<AIError, Continuation<T>, T> orElse = (err, cont) -> undefined();
        Function1<Continuation<T>, T> scope = (continuation) -> {
            AIKt.AIScope(runtime, block, orElse, continuation);
        };
        return LoomAdapter.apply(scope);
    }*/

    private <T> T undefined() {
        throw new RuntimeException("Method is undefined");
    }

    public <T> T prompt(
            String prompt,
            Class<T> cls,
            Integer maxAttempts,
            LLMModel llmModel,
            String user,
            Boolean echo,
            Integer n,
            Double temperature,
            Integer bringFromContext,
            Integer minResponseTokens
    ) {

        Function1<String, T> decoder = (json) -> {
            ObjectMapper om = new ObjectMapper();
            try {
                return om.readValue(json, cls);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };

        List<CFunction> functions = Collections.emptyList();

        try {
            return LoomAdapter.apply((continuation) ->
                    coreAIScope.promptWithSerializer(
                            prompt,
                            functions,
                            decoder,
                            maxAttempts,
                            llmModel,
                            user,
                            echo,
                            n,
                            temperature,
                            bringFromContext,
                            minResponseTokens,
                            continuation
                    )
            );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> promptMessage(
            String prompt,
            LLMModel llmModel,
            List<CFunction> functions,
            String user,
            Boolean echo,
            Integer n,
            Double temperature,
            Integer bringFromContext,
            Integer minResponseTokens
    ) {
        try {
            return LoomAdapter.apply((continuation) ->
                    coreAIScope.promptMessage(
                            prompt,
                            llmModel,
                            functions,
                            user,
                            echo,
                            n,
                            temperature,
                            bringFromContext,
                            minResponseTokens,
                            continuation));
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

    public List<String> images(
            String prompt,
            String user,
            String size,
            Integer bringFromContext,
            Integer n
    ) {
        try {
            ImagesGenerationResponse response = LoomAdapter.apply(continuation -> coreAIScope.images(
                    prompt,
                    user,
                    n,
                    size,
                    bringFromContext,
                    continuation
            ));

            List<String> listResponse = Collections.emptyList();
            for (ImageGenerationUrl e: response.getData()) {
                listResponse.add(e.getUrl());
            }

            return listResponse;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        coreAIScope.close();
    }
}
