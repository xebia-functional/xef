package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.auto.PlatformConversation;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;
import com.xebia.functional.xef.reasoning.serpapi.Search;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Markets {
    public String news;
    public List<String> raisingStockSymbols;
    public List<String> decreasingStockSymbols;

    @Override
    public String toString() {
        return "Markets{" +
              "news='" + news + '\'' +
              ", raisingStockSymbols=" + raisingStockSymbols +
              ", decreasingStockSymbols=" + decreasingStockSymbols +
              '}';
    }

    private static CompletableFuture<Void> stockMarketSummary(PlatformConversation scope) {
        Prompt news = new Prompt("Write a short summary of the stock market results given the provided context.");

        return scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, news, Markets.class)
              .thenAccept(markets -> System.out.println(markets));
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/M/yyyy");
            LocalDateTime now = LocalDateTime.now();
            String currentDate = dtf.format(now);
            Search search = new Search(OpenAI.FromEnvironment.DEFAULT_CHAT, scope, 3);
            scope.addContextFromArray(search.search(currentDate + "Stock market results, raising stocks, decreasing stocks").get());
            stockMarketSummary(scope).get();
        }
    }
}
