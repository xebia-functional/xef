package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.conversation.PlatformConversation;
import com.xebia.functional.xef.conversation.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class TopAttractions {

    public record TopAttraction(City city, String attractionName, String description, Weather weather){}
    public record City(String name, String country){}
    public record Weather(City city, Double temperature, String description){}

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt("Top attraction in Cádiz, Spain."), TopAttraction.class)
                .thenAccept(attraction -> System.out.println(
                    "The top attraction in " + attraction.city.name + " is " + attraction.attractionName + "." +
                    "Here's a brief description: " + attraction.description + "." +
                    "The weather in " + attraction.city.name + " is " +
                          (Objects.nonNull(attraction.weather) ? attraction.weather.temperature : null) +
                          " degrees Celsius and " + (Objects.nonNull(attraction.weather) ? attraction.weather.description : null) + "."
                    )
                ).get();
        }
    }

}
