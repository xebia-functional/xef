package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.java.auto.ExecutionContext;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Persons {

    public record Person(String name, int age){}

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope(new ExecutionContext(Executors.newVirtualThreadPerTaskExecutor()))) {
            scope.prompt("What is your name and age?", Person.class)
                    .thenAccept(person -> System.out.println(person))
                    .get();
        }
    }
}
