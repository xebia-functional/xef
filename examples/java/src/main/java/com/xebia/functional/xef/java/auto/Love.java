package com.xebia.functional.xef.java.auto;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Love {
    public List<String> loveList;
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            scope.prompt("tell me you like me with just emojis", Love.class)
                  .thenAccept(love -> System.out.println(love.loveList))
                  .get();
        }
    }
}
