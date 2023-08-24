package com.xebia.functional.xef.java.auto.jdk21.serialization;

import com.xebia.functional.xef.conversation.PlatformConversation;
import com.xebia.functional.xef.conversation.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;

import java.util.concurrent.ExecutionException;

public class Movies {

    public record Movie(String title, String genre, String director){}

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("Please provide a movie title, genre and director for the Inception movie"), Movie.class)
                    .thenAccept(movie -> System.out.println(movie))
                    .get();
        }
    }
}
