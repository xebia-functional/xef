package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.conversation.PlatformConversation;
import com.xebia.functional.xef.conversation.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;

import java.util.concurrent.ExecutionException;

public class Persons {

    public record Person(String name, int age){}

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("What is your name and age?"), Person.class)
                    .thenAccept(person -> System.out.println(person))
                    .get();
        }
    }
}
