package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.conversation.PlatformConversation;
import com.xebia.functional.xef.conversation.jvm.Description;
import com.xebia.functional.xef.conversation.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;

import java.util.concurrent.ExecutionException;

@Description("A book")
public class Book {

    @Description("Required. Produce a 50 word `summary` of this book.")
    public String summary;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("Produce a 50 word `summary` of `To Kill a Mockingbird` by Harper Lee summary."), Book.class)
                  .thenAccept(book ->
                          System.out.println("To Kill a Mockingbird summary:\n" + book.summary)
                  )
                  .get();
        }
    }

}
