package com.xebia.functional.xef.java.auto;

import java.util.concurrent.ExecutionException;

public class DivergentTasks {

    public Long numberOfMedicalNeedlesInWorld;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            scope.prompt("Provide the number of medical needles in the world", DivergentTasks.class)
                  .thenAccept(numberOfNeedles -> System.out.println("Needles in world:\n" + numberOfNeedles.numberOfMedicalNeedlesInWorld))
                  .get();
        }
    }

}
