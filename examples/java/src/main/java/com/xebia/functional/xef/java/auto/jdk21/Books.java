package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.java.auto.ExecutionContext;
import jakarta.validation.constraints.NotNull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Books {

    private final AIScope scope;

    public Books(AIScope scope) {
        this.scope = scope;
    }

    public record Book(@NotNull String title, @NotNull String author, @NotNull int year, @NotNull String genre){}

    public CompletableFuture<Books.Book> bookSelection(String topic) {
        return scope.prompt("Give me a selection of books about " + topic, Books.Book.class);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope(new ExecutionContext(Executors.newVirtualThreadPerTaskExecutor()))) {
            Books books = new Books(scope);
            books.bookSelection("artificial intelligence")
                    .thenAccept(System.out::println)
                    .get();
        }
    }
}
