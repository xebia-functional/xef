package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.conversation.PlatformConversation;
import com.xebia.functional.xef.conversation.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;
import com.xebia.functional.xef.reasoning.serpapi.Search;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class BreakingNews {

    public String summary;

    static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/M/yyyy");
    static LocalDateTime now = LocalDateTime.now();

    @Override
    public String toString() {
        return "BreakingNews{" +
              "summary='" + summary + '\'' +
              '}';
    }

    private static CompletableFuture<Void> writeParagraph(PlatformConversation scope) {
        String currentDate = dtf.format(now);

        return scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("write a paragraph of about 300 words about: " + currentDate + " Covid News"), BreakingNews.class)
              .thenAccept(breakingNews -> System.out.println(currentDate + " Covid news summary:\n" + breakingNews));
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            String currentDate = dtf.format(now);
            Search search = new Search(OpenAI.FromEnvironment.DEFAULT_CHAT, scope, 3);
            scope.addContextFromArray(search.search(currentDate + " Covid News").get());
            writeParagraph(scope).get();
        }
    }
}
