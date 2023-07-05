package com.xebia.functional.xef.java.auto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/M/yyyy");
            LocalDateTime now = LocalDateTime.now();
            String news = "|" +
                  "|Write a short summary of the stock market results given the provided context.";
            scope.prompt(news, Markets.class)
                  .thenAccept(markets -> System.out.println(markets))
                  .get();
        }
    }
}
