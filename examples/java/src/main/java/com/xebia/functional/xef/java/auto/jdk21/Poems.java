package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.java.auto.ExecutionContext;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Poems {
    public record Poem(String title, String content){}

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope(new ExecutionContext(Executors.newVirtualThreadPerTaskExecutor()))) {
            var poem1 = scope.prompt("A short poem about the beauty of nature.", Poem.class).get();
            var poem2 = scope.prompt("A short poem about the power of technology.", Poem.class).get();
            var poem3 = scope.prompt("A short poem about the wisdom of artificial intelligence.", Poem.class).get();

            var combinedPoems = String.format("%s\n\n%s\n\n%s", poem1.content, poem2.content, poem3.content);
            var newPoemPrompt = "Write a new poem that combines ideas from the following themes: the beauty " +
                    "of nature, the power of technology, and the wisdom of artificial intelligence. Here are some " +
                    "examples of poems on these themes: " + combinedPoems;

            scope.prompt(newPoemPrompt, Poem.class).
                    thenAccept(poem -> System.out.printf("New Poem:\n\n" + poem.content))
                    .get();
        }
    }
}
