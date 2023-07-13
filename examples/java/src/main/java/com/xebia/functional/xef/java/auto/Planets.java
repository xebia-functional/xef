package com.xebia.functional.xef.java.auto;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.toList;

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
        try (AIScope scope = new AIScope()) {
            CompletableFuture<Planet> earth = scope.prompt("Information about Earth and its moon.", Planet.class);
            CompletableFuture<Planet> mars = scope.prompt("Information about Mars and its moons.", Planet.class);

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


