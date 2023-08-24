package com.xebia.functional.xef.java.auto.jdk21.conversations;

import com.xebia.functional.xef.conversation.PlatformConversation;
import com.xebia.functional.xef.conversation.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.JvmPromptBuilder;
import com.xebia.functional.xef.prompt.Prompt;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Animals {

    private final PlatformConversation scope;

    public Animals(PlatformConversation scope) {
        this.scope = scope;
    }

    public CompletableFuture<Animal> uniqueAnimal() {
        return scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("A unique animal species."), Animal.class);
    }

    public CompletableFuture<Invention> groundbreakingInvention() {
        return scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("A groundbreaking invention from the 20th century."), Invention.class);
    }

    public CompletableFuture<String> story(Animal animal, Invention invention) {
        Prompt storyPrompt = new JvmPromptBuilder()
                .addSystemMessage("You are a writer for a science fiction magazine.")
                .addUserMessage("Write a short story of 200 words that involves the animal and the invention")
                .build();
        return scope.promptMessage(OpenAI.FromEnvironment.DEFAULT_CHAT, storyPrompt);
    }

    public record Animal(String name, String habitat, String diet){}
    public record Invention(String name, String inventor, int year, String purpose){}

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            Animals animals = new Animals(scope);
            animals.uniqueAnimal()
                    .thenCompose(animal ->
                          animals.groundbreakingInvention()
                            .thenCompose(invention ->
                                  animals.story(animal, invention)
                                    .thenAccept(System.out::println)
                            )).get();
        }
    }
}
