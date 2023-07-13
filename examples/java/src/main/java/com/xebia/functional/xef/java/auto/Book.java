package com.xebia.functional.xef.java.auto;

import java.util.concurrent.ExecutionException;

public class Book {

    public String title;
    public String author;
    public String summary;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            scope.prompt("To Kill a Mockingbird by Harper Lee summary.", Book.class)
                  .thenAccept(book -> System.out.println("To Kill a Mockingbird summary:\n" + book.summary))
                  .get();
        }
    }

}
