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

    private static final String pdfUrl = "https://people.cs.ksu.edu/~schmidt/705a/Scala/Programming-in-Scala.pdf";
    private static final BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));

    private static CompletableFuture<Void> askQuestion(AIScope scope) {
        System.out.println("Enter your question: ");

        if (readLine() == null) {
            return CompletableFuture.completedFuture(null);
        } else {
            scope.prompt(readLine(), AIResponse.class)
                    .thenAccept((aiRes) -> System.out.println(aiRes.answer + "\n---\n" +
                            aiRes.source + "\n---\n"));

            return askQuestion(scope);
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        TextSplitter textSplitter = TokenTextSplitter(ModelType.getDEFAULT_SPLITTER_MODEL(), 100, 50);
        try (AIScope scope = new AIScope()) {
            scope.contextScope(scope.pdf(pdfUrl, textSplitter).get(), PDFDocument::askQuestion).get();
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
