package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.auto.PlatformConversation;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;

import java.util.concurrent.ExecutionException;

public class Persons {

    public static class Person {
        public String name;
        public int age;

        @Override
        public String toString() {
            return "Hello " + name + ", you are " + age + " years old.";
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("What is your name and age?"), Person.class)
                    .thenAccept(person -> System.out.println(person))
                    .get();
        }
    }
}
