package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.auto.PlatformConversation;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;
import com.xebia.functional.xef.reasoning.serpapi.Search;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DivergentTasks {

    public Long numberOfMedicalNeedlesInWorld;

    private static CompletableFuture<Void> numberOfMedical(PlatformConversation scope) {
        return scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, "Provide the number of medical needles in the world", DivergentTasks.class)
              .thenAccept(numberOfNeedles -> System.out.println("Needles in world:\n" + numberOfNeedles.numberOfMedicalNeedlesInWorld));
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            Search search = new Search(OpenAI.FromEnvironment.DEFAULT_CHAT, scope, 3);
            scope.addContextFromArray(search.search("Estimate amount of medical needles in the world").get());
            numberOfMedical(scope).get();
        }
    }

}
