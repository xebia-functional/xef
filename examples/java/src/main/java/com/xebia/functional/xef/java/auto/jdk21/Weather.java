package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.conversation.PlatformConversation;
import com.xebia.functional.xef.conversation.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;
import com.xebia.functional.xef.reasoning.serpapi.Search;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Weather {
    public List<String> answer;

    private static CompletableFuture<Void> clothesRecommend(PlatformConversation scope) {
        return scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("Knowing this forecast, what clothes do you recommend I should wear?"), Weather.class)
                .thenAccept(weather ->
                        System.out.println(weather.answer)
                );
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            Search search = new Search(OpenAI.FromEnvironment.DEFAULT_CHAT, scope, 3);
            scope.addContextFromArray(search.search("Weather in Cádiz, Spain").get());
            clothesRecommend(scope).get();
        }
    }

}
