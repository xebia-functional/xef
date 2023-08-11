package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.java.auto.ExecutionContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class DivergentTasks {

    public Long numberOfMedicalNeedlesInWorld;

    private static CompletableFuture<Void> numberOfMedical(AIScope scope) {
        return scope.prompt("Provide the number of medical needles in the world", DivergentTasks.class)
              .thenAccept(numberOfNeedles -> System.out.println("Needles in world:\n" + numberOfNeedles.numberOfMedicalNeedlesInWorld));
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope(new ExecutionContext(Executors.newVirtualThreadPerTaskExecutor()))) {
            scope.addContext(scope.search("Estimate amount of medical needles in the world").get());
            numberOfMedical(scope).get();
        }
    }

}
