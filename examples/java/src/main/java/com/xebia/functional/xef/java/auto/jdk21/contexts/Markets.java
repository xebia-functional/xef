package com.xebia.functional.xef.java.auto.jdk21.contexts;

import com.xebia.functional.xef.conversation.PlatformConversation;
import com.xebia.functional.xef.conversation.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;
import com.xebia.functional.xef.reasoning.serpapi.Search;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Markets {

    public record Market(String news, List<String> raisingStockSymbols, List<String> decreasingStockSymbols) {
    }

    private static CompletableFuture<Void> stockMarketSummary(PlatformConversation scope) {
        var news = new Prompt("Write a short summary of the stock market results given the provided context.");

        return scope.prompt(OpenAI.fromEnvironment().DEFAULT_SERIALIZATION, news, Market.class)
                .thenAccept(markets -> System.out.println(markets));
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            var dtf = DateTimeFormatter.ofPattern("dd/M/yyyy");
            var now = LocalDateTime.now();
            var currentDate = dtf.format(now);
            var search = new Search(OpenAI.fromEnvironment().DEFAULT_CHAT, scope, 3);
            scope.addContextFromArray(search.search(currentDate + "Stock market results, raising stocks, decreasing stocks").get());
            stockMarketSummary(scope).get();
        }
    }
}
