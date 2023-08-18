package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.conversation.PlatformConversation;
import com.xebia.functional.xef.conversation.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MeaningOfLife {
    public List<String> mainTheories;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("What are the main theories about the meaning of life"), MeaningOfLife.class)
                  .thenAccept(meaningOfLife ->
                        System.out.println("There are several theories about the meaning of life:\n" + meaningOfLife.mainTheories))
                  .get();
        }
    }
}
