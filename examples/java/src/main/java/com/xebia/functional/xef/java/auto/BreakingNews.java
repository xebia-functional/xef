package com.xebia.functional.xef.java.auto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class BreakingNews {

    public String summary;

    @Override
    public String toString() {
        return "BreakingNews{" +
              "summary='" + summary + '\'' +
              '}';
    }

    private static CompletableFuture<Void> writeParagraph(AIScope scope) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/M/yyyy");
        LocalDateTime now = LocalDateTime.now();
        var currentDate = dtf.format(now);

        return scope.prompt("write a paragraph of about 300 words about: " + currentDate + " Covid News", BreakingNews.class)
              .thenAccept(breakingNews -> System.out.println(currentDate + " Covid news summary:\n" + breakingNews));
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/M/yyyy");
            LocalDateTime now = LocalDateTime.now();
            var currentDate = dtf.format(now);
            scope.contextScope(scope.search(currentDate + " Covid News").get(), BreakingNews::writeParagraph).get();
        }
    }
}
