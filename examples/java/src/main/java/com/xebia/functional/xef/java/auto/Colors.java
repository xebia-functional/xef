package com.xebia.functional.xef.java.auto;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Colors {

    public List<String> colors;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            scope.prompt("a selection of 10 beautiful colors that go well together", Colors.class)
                  .thenAccept(colors -> System.out.println("Colors:\n" + colors.colors))
                  .get();
        }
    }
}
