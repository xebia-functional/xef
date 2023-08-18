package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.auto.PlatformConversation;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;

import java.util.concurrent.ExecutionException;

public class Fact {

    public record FactRecord(String topic, String content) {
    }

    public record Riddle(FactRecord fact1, FactRecord fact2, String riddle) {
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            var fact1 = scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("A fascinating fact about you"), FactRecord.class).get();
            var fact2 = scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("An interesting fact about me"), FactRecord.class).get();

            var riddlePrompt = new Prompt(
                    "Create a riddle that combines the following facts:\n\n" +

                            "Fact 1: " + fact1.content + "\n" +
                            "Fact 2: " + fact2.content);

            scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, riddlePrompt, Riddle.class)
                    .thenAccept(riddle -> System.out.println("Riddle:\n\n" + riddle)).get();
        }
    }

}
