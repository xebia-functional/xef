package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.java.auto.ExecutionContext;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class TouristAttractions {

    public record TouristAttraction(String name, String location, String history){}

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope(new ExecutionContext(Executors.newVirtualThreadPerTaskExecutor()))) {
            scope.prompt("Statue of Liberty location and history.", TouristAttraction.class)
                    .thenAccept(statueOfLiberty -> System.out.println(
                            statueOfLiberty.name + "is located in " + statueOfLiberty.location +
                                    " and has the following history: " + statueOfLiberty.history
                            )
                    ).get();
        }
    }
}
