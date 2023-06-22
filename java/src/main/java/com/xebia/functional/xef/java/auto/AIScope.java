package com.xebia.functional.xef.java.auto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xebia.functional.loom.LoomAdapter;
import com.xebia.functional.xef.auto.CoreAIScope;
import com.xebia.functional.xef.llm.openai.LLMModel;
import com.xebia.functional.xef.llm.openai.functions.CFunction;
import kotlin.jvm.functions.Function1;

import java.util.Collections;
import java.util.List;

public class AIScope implements AutoCloseable {
    private CoreAIScope coreAIScope;

    private <T> T undefined() {
        throw new RuntimeException("Method is undefined");
    }

    public <T> T prompt(
            String prompt,
            Class<T> cls,
            int maxAttempts,
            LLMModel llmModel,
            String user,
            Boolean echo,
            int n,
            Double temperature,
            int bringFromContext,
            int minResponseTokens
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

    @Override
    public void close() throws Exception {}
}
