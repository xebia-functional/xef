package com.xebia.functional.xef.java.auto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

public class BreakingNews {

    public String summary;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/M/yyyy");
            LocalDateTime now = LocalDateTime.now();
            scope.prompt("write a paragraph of about 300 words about: " + dtf.format(now) + " Covid News", BreakingNews.class)
                  .thenAccept(breakingNews -> System.out.println(dtf.format(now) + " Covid news summary:\n" + breakingNews.summary))
                  .get();
        }
    }
}
