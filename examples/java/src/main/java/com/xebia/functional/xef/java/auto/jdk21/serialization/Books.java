package com.xebia.functional.xef.java.auto.jdk21.serialization;

import com.xebia.functional.xef.conversation.PlatformConversation;
import com.xebia.functional.xef.conversation.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;
import jakarta.validation.constraints.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Books {

    private final PlatformConversation scope;

    public Books(PlatformConversation scope) {
        this.scope = scope;
    }

    public record Book(@NotNull String title, @NotNull String author, @NotNull int year, @NotNull String genre){}

    public CompletableFuture<Books.Book> bookSelection(String topic) {
        return scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("Give me a selection of books about " + topic), Books.Book.class);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            Books books = new Books(scope);
            books.bookSelection("artificial intelligence")
                    .thenAccept(System.out::println)
                    .get();
        }
    }
}
