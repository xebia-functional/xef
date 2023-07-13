package com.xebia.functional.xef.java.auto;

import com.xebia.functional.tokenizer.ModelType;
import com.xebia.functional.xef.textsplitters.TextSplitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.xebia.functional.xef.textsplitters.TokenTextSplitterKt.TokenTextSplitter;

public class PDFDocument {

    public static class AIResponse {
        public String answer;
        public String source;
    }

    private static final String PDF_URL = "https://people.cs.ksu.edu/~schmidt/705a/Scala/Programming-in-Scala.pdf";
    private static final BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));

    private static CompletableFuture<Void> askQuestion(AIScope scope) {
        System.out.println("Enter your question (<return> to exit): ");

        String line = readLine();
        if (line == null || line.isBlank()) {
            return CompletableFuture.completedFuture(null);
        } else {
            scope.prompt(line, AIResponse.class)
                    .thenAccept(aiRes -> System.out.println(aiRes.answer + "\n---\n" +
                            aiRes.source + "\n---\n"));

            return askQuestion(scope);
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        TextSplitter textSplitter = TokenTextSplitter(ModelType.getDEFAULT_SPLITTER_MODEL(), 100, 50);
        try (AIScope scope = new AIScope()) {
            scope.contextScope(scope.pdf(PDF_URL, textSplitter).get(), PDFDocument::askQuestion).get();
        }
    }

    private static String readLine() {
        try {
            return sysin.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}
