package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.auto.PlatformConversation;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MeaningOfLife {
    public List<String> mainTheories;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, "What are the main theories about the meaning of life", MeaningOfLife.class)
                  .thenAccept(meaningOfLife ->
                        System.out.println("There are several theories about the meaning of life:\n" + meaningOfLife.mainTheories))
                  .get();
        }
    }
}
