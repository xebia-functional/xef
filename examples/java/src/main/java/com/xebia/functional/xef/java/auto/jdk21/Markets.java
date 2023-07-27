package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.java.auto.ExecutionContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Markets {

    public record Market(String news, List<String> raisingStockSymbols, List<String> decreasingStockSymbols){}

    private static CompletableFuture<Void> stockMarketSummary(AIScope scope) {
        var news = "|" +
              "|Write a short summary of the stock market results given the provided context.";

        return scope.prompt(news, Market.class)
              .thenAccept(markets -> System.out.println(markets));
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope(new ExecutionContext(Executors.newVirtualThreadPerTaskExecutor()))) {
            var dtf = DateTimeFormatter.ofPattern("dd/M/yyyy");
            var now = LocalDateTime.now();
            var currentDate = dtf.format(now);

            scope.contextScope(scope.search(currentDate + "Stock market results, raising stocks, decreasing stocks"),
                  Markets::stockMarketSummary).get();
        }
    }
}
