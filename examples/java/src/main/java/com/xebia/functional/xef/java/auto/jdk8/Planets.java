package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.auto.PlatformConversation;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Planets {
    static class Planet {
        public String name;
        public double distanceFromSun;
        public List<Moon> moons;
    }

    static class Moon {
        public String name;
        public double distanceFromPlanetInKm;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            CompletableFuture<Planet> earth = scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, "Information about Earth and its moon.", Planet.class);
            CompletableFuture<Planet> mars = scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, "Information about Mars and its moons.", Planet.class);

            System.out.println("Celestial bodies information:\n\n" + planetInfo(earth.get()) + "\n\n" + planetInfo(mars.get()));
        }
    }

    private static String planetInfo(Planet planet){
        List<String> moonList = planet.moons.stream().map(it -> "  - " + it.name + ": " + it.distanceFromPlanetInKm + " km away from " + planet.name).toList();

        return String.format("%s is %s million km away from the Sun.\n" +
                "It has the following moons: \n" +
                "%s", planet.name, planet.distanceFromSun, String.join("\n", moonList));
    }
}


