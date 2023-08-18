package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.auto.PlatformConversation;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;

import java.util.concurrent.ExecutionException;

public class Book {

    public String title;
    public String author;
    public String summary;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("To Kill a Mockingbird by Harper Lee summary."), Book.class)
                  .thenAccept(book -> System.out.println("To Kill a Mockingbird summary:\n" + book.summary))
                  .get();
        }
    }

}
