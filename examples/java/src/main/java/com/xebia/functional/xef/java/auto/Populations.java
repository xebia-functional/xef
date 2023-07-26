package com.xebia.functional.xef.java.auto;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Populations {

    static class Population {
        public int population;
        public String description;
    }

    static class Image {
        public String description;
        public String url;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            CompletableFuture<? extends Population> cadiz = scope.prompt("What is the population of Cádiz, Spain.", Population.class);
            CompletableFuture<? extends Population> seattle = scope.prompt("What is the population of Seattle, WA.", Population.class);
            CompletableFuture<? extends Image> img = scope.prompt("A hybrid city of Cádiz, Spain and Seattle, US.", Image.class);
            System.out.println(img.get());
            System.out.println("The population of Cádiz is " + cadiz.get().population + " and the population of Seattle is " + seattle.get().population);
        }
    }
}
