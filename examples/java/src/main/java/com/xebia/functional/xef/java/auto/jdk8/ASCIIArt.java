package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.java.auto.AIScope;
import java.util.concurrent.ExecutionException;

public class ASCIIArt {
    public String art;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            scope.prompt("ASCII art of a cat dancing", ASCIIArt.class)
                    .thenAccept(art -> System.out.println(art.art))
                    .get();
        }
    }
}
