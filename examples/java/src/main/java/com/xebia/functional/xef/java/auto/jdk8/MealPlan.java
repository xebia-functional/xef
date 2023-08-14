package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.auto.PlatformConversation;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;
import com.xebia.functional.xef.reasoning.serpapi.Search;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MealPlan {
    public String name;
    public List<Recipe> recipes;

    private static class Recipe {
        public String name;
        public List<String> ingredients;

        @Override
        public String toString() {
            return "Recipe{" +
                    "name='" + name + '\'' +
                    ", ingredients=" + ingredients +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "MealPlan{" +
                "name='" + name + '\'' +
                ", recipes=" + recipes +
                '}';
    }

    private static CompletableFuture<Void> mealPlan(PlatformConversation scope) {
        return scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, "Meal plan for the week for a person with gall bladder stones that includes 5 recipes.", MealPlan.class)
                .thenAccept(mealPlan -> System.out.println(mealPlan));
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            Search search = new Search(OpenAI.FromEnvironment.DEFAULT_CHAT, scope, 3);
            scope.addContextFromArray(search.search("gall bladder stones meals").get());
            mealPlan(scope).get();
        }
    }
}
