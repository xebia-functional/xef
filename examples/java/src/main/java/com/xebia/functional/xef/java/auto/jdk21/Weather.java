package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.java.auto.ExecutionContext;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Weather {
    public List<String> answer;

    private static CompletableFuture<Void> clothesRecommend(AIScope scope) {
        return scope.prompt("Knowing this forecast, what clothes do you recommend I should wear?", Weather.class)
                .thenAccept(weather ->
                        System.out.println(weather.answer)
                );
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope(new ExecutionContext(Executors.newVirtualThreadPerTaskExecutor()))) {
            scope.addContext(scope.search("Weather in Cádiz, Spain").get());
            clothesRecommend(scope).get();
        }
    }

}
