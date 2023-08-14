package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.auto.PlatformConversation;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;
import jakarta.validation.constraints.NotNull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Books {

    private final PlatformConversation scope;

    public Books(PlatformConversation scope) {
        this.scope = scope;
    }

    public static class Book {
        @NotNull public String title;
        @NotNull public String author;
        @NotNull public int year;
        @NotNull public String genre;

        @Override
        public String toString() {
            return "Book{" +
                    "title='" + title + '\'' +
                    ", author='" + author + '\'' +
                    ", year=" + year +
                    ", genre='" + genre + '\'' +
                    '}';
        }
    }

    public CompletableFuture<Book> bookSelection(String topic) {
        return scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, "Give me a selection of books about " + topic, Book.class);
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
