package com.xebia.functional.xef.java.auto;

import java.util.concurrent.ExecutionException;

public class TopAttractions {

    static class TopAttraction {
        public City city;
        public String attractionName;
        public String description;
        public Weather weather;
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
        try (AIScope scope = new AIScope()) {
            scope.prompt("Top attraction in Cádiz, Spain.", TopAttractions.TopAttraction.class)
                .thenAccept((attraction) -> System.out.println(
                    "The top attraction in " + attraction.city.name + " is " + attraction.attractionName + "." +
                    "Here's a brief description: " + attraction.description + "." +
                    "The weather in " + attraction.city.name + " is " + attraction.weather.temperature + " degrees Celsius and " + attraction.weather.description + "."
                    )
                ).get();
        }
    }

}
