package com.xebia.functional.xef.java.auto;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Recipes {

    static class Recipe {
        public String name;
        public List<String> ingredients;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            Recipe recipe = scope.prompt("Recipe for chocolate chip cookies.", Recipe.class).get();
            System.out.println("The recipe for " + recipe.name + " is " + recipe.ingredients );
        }
    }
}
