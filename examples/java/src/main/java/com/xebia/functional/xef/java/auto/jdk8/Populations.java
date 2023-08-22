package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.conversation.PlatformConversation;
import com.xebia.functional.xef.conversation.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;

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
        try (PlatformConversation scope = OpenAI.conversation()) {
            CompletableFuture<Population> cadiz = scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("What is the population of Cádiz, Spain."), Population.class);
            CompletableFuture<Population> seattle = scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("What is the population of Seattle, WA."), Population.class);
            CompletableFuture<Image> img = scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("A hybrid city of Cádiz, Spain and Seattle, US."), Image.class);
            System.out.println(img.get());
            System.out.println("The population of Cádiz is " + cadiz.get().population + " and the population of Seattle is " + seattle.get().population);
        }
    }
}
