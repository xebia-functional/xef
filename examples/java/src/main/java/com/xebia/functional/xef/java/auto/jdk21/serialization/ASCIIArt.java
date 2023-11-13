package com.xebia.functional.xef.java.auto.jdk21.serialization;

import com.xebia.functional.xef.conversation.PlatformConversation;
import com.xebia.functional.xef.conversation.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;

import java.util.concurrent.ExecutionException;

public class ASCIIArt {
    public String art;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            scope.prompt(OpenAI.fromEnvironment().DEFAULT_SERIALIZATION, new Prompt("ASCII art of a cat dancing"), ASCIIArt.class)
                    .thenAccept(art -> System.out.println(art.art))
                    .get();
        }
    }
}
