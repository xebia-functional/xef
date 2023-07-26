package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.java.auto.ExecutionContext;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class ASCIIArt {
    public String art;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope(new ExecutionContext(Executors.newVirtualThreadPerTaskExecutor()))) {
            scope.prompt("ASCII art of a cat dancing", ASCIIArt.class)
                    .thenAccept(art -> System.out.println(art.art))
                    .get();
        }
    }
}
