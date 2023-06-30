package com.xebia.functional.xef.java.auto;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Weather {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
//            scope.contextScope(search())

//           DefaultSearchKt.search("abc")

            scope.prompt("Knowing this forecast, what clothes do you recommend I should wear?", List.class)
                    .thenAccept((list) -> System.out.println(
                                    list
                            )
                    ).get();
        }
    }

}
