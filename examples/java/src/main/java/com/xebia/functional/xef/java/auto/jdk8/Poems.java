package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.conversation.PlatformConversation;
import com.xebia.functional.xef.conversation.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Poems {
    public static class Poem {
        public String title;
        public String content;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            CompletableFuture<Poem> poem1 = scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("A short poem about the beauty of nature."), Poem.class);
            CompletableFuture<Poem> poem2 = scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("A short poem about the power of technology."), Poem.class);
            CompletableFuture<Poem> poem3 = scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("A short poem about the wisdom of artificial intelligence."), Poem.class);

            String combinedPoems = String.format("%s\n\n%s\n\n%s", poem1.get().content, poem2.get().content, poem3.get().content);
            Prompt newPoemPrompt = new Prompt("Write a new poem that combines ideas from the following themes: the beauty " +
                    "of nature, the power of technology, and the wisdom of artificial intelligence. Here are some " +
                    "examples of poems on these themes: " + combinedPoems);

            scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, newPoemPrompt, Poem.class).
                    thenAccept(poem -> System.out.printf("New Poem:\n\n" + poem.content))
                    .get();
        }
    }
}
