package com.xebia.functional.xef.java.auto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class BreakingNews {

    public String summary;

    static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/M/yyyy");
    static LocalDateTime now = LocalDateTime.now();


    private static CompletableFuture<Void> writeParagraph(AIScope scope) {
        var currentDate = dtf.format(now);

        scope.prompt("write a paragraph of about 300 words about: " + currentDate + " Covid News", BreakingNews.class)
              .thenAccept(breakingNews -> System.out.println(currentDate + " Covid news summary:\n" + breakingNews.summary));

        return writeParagraph(scope);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            var currentDate = dtf.format(now);
            scope.contextScope(scope.search(currentDate + " Covid News").get(), BreakingNews::writeParagraph).get();
        }
    }
}
