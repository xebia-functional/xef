package com.xebia.functional.xef.java.auto;

import com.xebia.functional.xef.agents.DefaultSearchKt;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.ExecutorsKt;
import kotlinx.coroutines.JobKt;
import kotlinx.coroutines.future.FutureKt;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

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
