package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.auto.PlatformConversation;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Colors {

    public List<String> colors;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, "a selection of 10 beautiful colors that go well together", Colors.class)
                  .thenAccept(colors -> System.out.println("Colors:\n" + colors.colors))
                  .get();
        }
    }
}
