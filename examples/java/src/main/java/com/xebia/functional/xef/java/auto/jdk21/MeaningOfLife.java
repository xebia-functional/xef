package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.java.auto.AIScope;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MeaningOfLife {
    public List<String> mainTheories;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            scope.prompt("What are the main theories about the meaning of life", MeaningOfLife.class)
                  .thenAccept(meaningOfLife ->
                        System.out.println("There are several theories about the meaning of life:\n" + meaningOfLife.mainTheories))
                  .get();
        }
    }
}
