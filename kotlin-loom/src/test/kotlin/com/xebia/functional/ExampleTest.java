package com.xebia.functional;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ExampleTest {

    @Test
    public void isCancelled() throws InterruptedException {
        CompletableFuture<Throwable> completable = new CompletableFuture<>();
        Thread t = Thread.ofVirtual().start(() -> {
            try {
                LoomAdapter.apply(TestFunctions.forever(completable));
            } catch (InterruptedException e) {
                // do nothing
            }
        });
        Thread.sleep(1000);
        t.interrupt();
        Throwable exitCase = completable.join();
        assertThat(exitCase).isInstanceOf(CancellationException.class);
    }

    @Test
    public void isCompleted() throws InterruptedException {
        int result = LoomAdapter.apply(TestFunctions.completed(1));
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void isFailure() {
        assertThatThrownBy(() -> {
            LoomAdapter.apply(TestFunctions.failure(new RuntimeException("Boom!")));
        }).isInstanceOf(RuntimeException.class).hasMessage("Boom!");
    }

    @Test
    public void arity1() throws InterruptedException {
        int result = LoomAdapter.apply(1, TestFunctions.completed());
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void arity2() throws InterruptedException {
        int result = LoomAdapter.apply(1, 2, TestFunctions.combine(Integer::sum));
        assertThat(result).isEqualTo(3);
    }
}
