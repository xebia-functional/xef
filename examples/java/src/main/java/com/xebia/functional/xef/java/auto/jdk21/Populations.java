package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.java.auto.ExecutionContext;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class Populations {

    public record Population(int population, String description){}
    public record Image(String description, String url){}

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope(new ExecutionContext(Executors.newVirtualThreadPerTaskExecutor()))) {
            var cadiz = scope.prompt("What is the population of Cádiz, Spain.", Population.class).get();
            var seattle = scope.prompt("What is the population of Seattle, WA.", Population.class).get();
            var img = scope.prompt("A hybrid city of Cádiz, Spain and Seattle, US.", Image.class).get();
            System.out.println(img);
            System.out.println("The population of Cádiz is " + cadiz.population + " and the population of Seattle is " + seattle.population);
        }
    }
}
