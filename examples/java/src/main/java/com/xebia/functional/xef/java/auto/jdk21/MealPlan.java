package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.java.auto.ExecutionContext;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class MealPlan {

    public record MealPlanRecord(String name, List<Recipe> recipes){}
    public record Recipe(String name, List<String> ingredients){}

    private static CompletableFuture<Void> mealPlan(AIScope scope) {
        return scope.prompt("Meal plan for the week for a person with gall bladder stones that includes 5 recipes.", MealPlanRecord.class)
              .thenAccept(mealPlan -> System.out.println(mealPlan));
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope(new ExecutionContext(Executors.newVirtualThreadPerTaskExecutor()))) {
            scope.contextScope(scope.search("gall bladder stones meals"), MealPlan::mealPlan).get();
        }
    }
}
