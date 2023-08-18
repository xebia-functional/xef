package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.auto.PlatformConversation;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Recipes {

    static class Recipe {
        public String name;
        public List<String> ingredients;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            Recipe recipe = scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("Recipe for chocolate chip cookies."), Recipe.class).get();
            System.out.println("The recipe for " + recipe.name + " is " + recipe.ingredients );
        }
    }
}
