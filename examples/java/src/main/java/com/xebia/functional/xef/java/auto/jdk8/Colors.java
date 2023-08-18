package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.conversation.PlatformConversation;
import com.xebia.functional.xef.conversation.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Colors {

    public List<String> colors;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        PlatformConversation scope = OpenAI.conversation();
        scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("a selection of 10 beautiful colors that go well together"), Colors.class)
                  .thenAccept(colors -> System.out.println("Colors:\n" + colors.colors))
                  .get();

    }
}
