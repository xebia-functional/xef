package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.auto.PlatformConversation;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Love {
    public List<String> loveList;
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, "tell me you like me with just emojis", Love.class)
                  .thenAccept(love -> System.out.println(love.loveList))
                  .get();
        }
    }
}
