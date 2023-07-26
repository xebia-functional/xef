package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.java.auto.AIScope;
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

    private static CompletableFuture<Void> stockMarketSummary(AIScope scope) {
        String news = "|" +
              "|Write a short summary of the stock market results given the provided context.";

        return scope.prompt(news, Markets.class)
              .thenAccept(markets -> System.out.println(markets));
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/M/yyyy");
            LocalDateTime now = LocalDateTime.now();
            String currentDate = dtf.format(now);

            scope.contextScope(scope.search(currentDate + "Stock market results, raising stocks, decreasing stocks"),
                  Markets::stockMarketSummary).get();
        }
    }
}
