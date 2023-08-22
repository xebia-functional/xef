package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.conversation.PlatformConversation;
import com.xebia.functional.xef.conversation.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;

import java.util.concurrent.ExecutionException;

public class TopAttractions {

    static class TopAttraction {
        public City city;
        public String attractionName;
        public String description;
        public Weather weather = new Weather();
    }

    static class City {
        public String name;
        public String country;
    }

    static class Weather {
        public City city;
        public double temperature;
        public String description;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("Top attraction in Cádiz, Spain."), TopAttraction.class)
                .thenAccept(attraction -> System.out.println(
                    "The top attraction in " + attraction.city.name + " is " + attraction.attractionName + "." +
                    "Here's a brief description: " + attraction.description + "." +
                    "The weather in " + attraction.city.name + " is " + attraction.weather.temperature + " degrees Celsius and " + attraction.weather.description + "."
                    )
                ).get();
        }
    }

}
