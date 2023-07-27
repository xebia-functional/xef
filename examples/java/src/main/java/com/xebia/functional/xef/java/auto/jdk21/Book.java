package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.java.auto.ExecutionContext;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Book {

    public String title;
    public String author;
    public String summary;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope(new ExecutionContext(Executors.newVirtualThreadPerTaskExecutor()))) {
            scope.prompt("To Kill a Mockingbird by Harper Lee summary.", Book.class)
                  .thenAccept(book -> System.out.println("To Kill a Mockingbird summary:\n" + book.summary))
                  .get();
        }
    }

}
