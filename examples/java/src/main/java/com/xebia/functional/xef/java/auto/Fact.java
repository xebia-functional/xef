package com.xebia.functional.xef.java.auto;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Fact {

    private final AIScope scope;

    public Fact(AIScope scope) {
        this.scope = scope;
    }

    public String topic;
    public String content;

    private static class Riddle {
        public Fact fact1;
        public Fact fact2;
        public String riddle;
    }

    public CompletableFuture<Fact> firstFact() {
        return scope.prompt("A fascinating fact about you", Fact.class);
    }

    public CompletableFuture<Fact> secondFact() {
        return scope.prompt("An interesting fact about me", Fact.class);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            Fact fact = new Fact(scope);
            fact.firstFact()
                  .thenCompose(fact1 -> fact1.secondFact()
                        .thenCompose(fact2 -> {
                            String riddlePrompt = ""+
                                "Create a riddle that combines the following facts:" +

                                "Fact 1: " + fact1.content +
                                "Fact 2: " + fact2.content;
                                return fact.scope.prompt(riddlePrompt, Riddle.class)
                                      .thenAccept(riddle -> System.out.println("Riddle:\n\n" + riddle));
                        })
                        ).get();
        }
    }

}
