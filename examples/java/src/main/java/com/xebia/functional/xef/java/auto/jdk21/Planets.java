package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.java.auto.ExecutionContext;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Planets {
    public record Planet(String name, double distanceFromSun, List<Moon> moons){}
    public record Moon(String name, double distanceFromPlanetInKm){}

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope(new ExecutionContext(Executors.newVirtualThreadPerTaskExecutor()))) {
            var earth = scope.prompt("Information about Earth and its moon.", Planet.class).get();
            var mars = scope.prompt("Information about Mars and its moons.", Planet.class).get();

            System.out.println("Celestial bodies information:\n\n" + planetInfo(earth) + "\n\n" + planetInfo(mars));
        }
    }

    private static String planetInfo(Planet planet){
        var moonList = planet.moons.stream().map(it -> "  - " + it.name + ": " + it.distanceFromPlanetInKm + " km away from " + planet.name).toList();

        return String.format("%s is %s million km away from the Sun.\n" +
                "It has the following moons: \n" +
                "%s", planet.name, planet.distanceFromSun, String.join("\n", moonList));
    }
}


