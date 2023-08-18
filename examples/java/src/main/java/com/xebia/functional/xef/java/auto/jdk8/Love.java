package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.conversation.PlatformConversation;
import com.xebia.functional.xef.conversation.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Love {
    public List<String> loveList;
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("tell me you like me with just emojis"), Love.class)
                  .thenAccept(love -> System.out.println(love.loveList))
                  .get();
        }
    }
}
