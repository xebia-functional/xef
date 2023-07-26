package com.xebia.functional.xef.java.auto;

import jakarta.validation.constraints.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Books {

    private final AIScope scope;

    public Books(AIScope scope) {
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

    public CompletableFuture<? extends Books.Book> bookSelection(String topic) {
        return scope.prompt("Give me a selection of books about " + topic, Books.Book.class);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            Books books = new Books(scope);
            books.bookSelection("artificial intelligence")
                    .thenAccept(System.out::println)
                    .get();
        }
    }
}
