package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.java.auto.AIScope;
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

    private static CompletableFuture<Void> writeParagraph(AIScope scope) {
        String currentDate = dtf.format(now);

        return scope.prompt("write a paragraph of about 300 words about: " + currentDate + " Covid News", BreakingNews.class)
              .thenAccept(breakingNews -> System.out.println(currentDate + " Covid news summary:\n" + breakingNews));
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            String currentDate = dtf.format(now);
            scope.addContext(scope.search(currentDate + " Covid News").get());
            writeParagraph(scope).get();
        }
    }
}
