package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.java.auto.ExecutionContext;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Recipes {

    public record Recipe(String name, List<String> ingredients){}

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope(new ExecutionContext(Executors.newVirtualThreadPerTaskExecutor()))) {
            var recipe = scope.prompt("Recipe for chocolate chip cookies.", Recipe.class).get();
            System.out.println("The recipe for " + recipe.name + " is " + recipe.ingredients );
        }
    }
}
