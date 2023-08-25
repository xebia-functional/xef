package com.xebia.functional.xef.java.auto.jdk21.serialization;

import com.xebia.functional.xef.conversation.PlatformConversation;
import com.xebia.functional.xef.conversation.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Recipes {

    public record Recipe(String name, List<String> ingredients){}

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            var recipe = scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("Recipe for chocolate chip cookies."), Recipe.class).get();
            System.out.println("The recipe for " + recipe.name + " is " + recipe.ingredients );
        }
    }
}
