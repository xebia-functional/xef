package com.xebia.functional.xef.java.auto;

import java.util.List;
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

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            scope.prompt("Meal plan for the week for a person with gall bladder stones that includes 5 recipes.", MealPlan.class)
                  .thenAccept(mealPlan -> System.out.println(mealPlan))
                  .get();
        }
    }
}
