package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.auto.PlatformConversation;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;

import java.util.concurrent.ExecutionException;

public class Fact {

    private static class FactClass {
        public String topic;
        public String content;

        @Override
        public String toString() {
            return "FactClass{" +
                  "topic='" + topic + '\'' +
                  ", content='" + content + '\'' +
                  '}';
        }
    }

    private static class Riddle {
        public FactClass fact1;
        public FactClass fact2;
        public String riddle;

        @Override
        public String toString() {
            return "Riddle{" +
                  "fact1=" + fact1 +
                  ", fact2=" + fact2 +
                  ", riddle='" + riddle + '\'' +
                  '}';
        }
    }



    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            FactClass fact1 = scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, "A fascinating fact about you", FactClass.class).get();
            FactClass fact2 = scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, "An interesting fact about me", FactClass.class).get();

            String riddlePrompt = ""+
                "Create a riddle that combines the following facts:\n\n" +

                "Fact 1: " + fact1.content + "\n" +
                "Fact 2: " + fact2.content;

            scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, riddlePrompt, Riddle.class)
                  .thenAccept(riddle -> System.out.println("Riddle:\n\n" + riddle)).get();
        }
    }

}
