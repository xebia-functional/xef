package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.auto.PlatformConversation;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;

import java.util.concurrent.ExecutionException;

public class Populations {

    public record Population(int population, String description){}
    public record Image(String description, String url){}

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            var cadiz = scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, "What is the population of Cádiz, Spain.", Population.class).get();
            var seattle = scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, "What is the population of Seattle, WA.", Population.class).get();
            var img = scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, "A hybrid city of Cádiz, Spain and Seattle, US.", Image.class).get();
            System.out.println(img);
            System.out.println("The population of Cádiz is " + cadiz.population + " and the population of Seattle is " + seattle.population);
        }
    }
}
