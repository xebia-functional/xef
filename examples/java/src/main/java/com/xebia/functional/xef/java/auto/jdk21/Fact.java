package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.java.auto.ExecutionContext;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Fact {

    public record FactRecord(String topic, String content){}
    public record Riddle(FactRecord fact1, FactRecord fact2, String riddle){}

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope(new ExecutionContext(Executors.newVirtualThreadPerTaskExecutor()))) {
            var fact1 = scope.prompt("A fascinating fact about you", FactRecord.class).get();
            var fact2 = scope.prompt("An interesting fact about me", FactRecord.class).get();

            var riddlePrompt = ""+
                "Create a riddle that combines the following facts:\n\n" +

                "Fact 1: " + fact1.content + "\n" +
                "Fact 2: " + fact2.content;

            scope.prompt(riddlePrompt, Riddle.class)
                  .thenAccept(riddle -> System.out.println("Riddle:\n\n" + riddle)).get();
        }
    }

}
