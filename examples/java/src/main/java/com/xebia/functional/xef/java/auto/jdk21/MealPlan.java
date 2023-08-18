package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.auto.PlatformConversation;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;
import com.xebia.functional.xef.reasoning.serpapi.Search;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MealPlan {

    public record MealPlanRecord(String name, List<Recipe> recipes){}
    public record Recipe(String name, List<String> ingredients){}

    private static CompletableFuture<Void> mealPlan(PlatformConversation scope) {
        return scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("Meal plan for the week for a person with gall bladder stones that includes 5 recipes."), MealPlanRecord.class)
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
