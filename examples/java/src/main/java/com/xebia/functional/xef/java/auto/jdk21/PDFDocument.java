package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.tokenizer.ModelType;
import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.java.auto.jdk21.util.ConsoleUtil;
import com.xebia.functional.xef.textsplitters.TextSplitter;

import java.util.concurrent.CompletableFuture;

import static com.xebia.functional.xef.textsplitters.TokenTextSplitterKt.TokenTextSplitter;

public class PDFDocument {

    private static ConsoleUtil util = new ConsoleUtil();

    public static class AIResponse {
        public String answer;
        public String source;
    }

    private static final String PDF_URL = "https://people.cs.ksu.edu/~schmidt/705a/Scala/Programming-in-Scala.pdf";

    private static CompletableFuture<Void> askQuestion(AIScope scope) {
        System.out.println("Enter your question (<return> to exit): ");


        String line = util.readLine();
        if (line == null || line.isBlank()) {
            return CompletableFuture.completedFuture(null);
        } else {
            scope.prompt(line, AIResponse.class)
                    .thenAccept(aiRes -> System.out.println(aiRes.answer + "\n---\n" +
                            aiRes.source + "\n---\n"));

            return askQuestion(scope);
        }
    }

    public static void main(String[] args) throws Exception {

        TextSplitter textSplitter = TokenTextSplitter(ModelType.getDEFAULT_SPLITTER_MODEL(), 100, 50);
        try (AIScope scope = new AIScope()) {
            scope.contextScope(scope.pdf(PDF_URL, textSplitter), PDFDocument::askQuestion).get();
        }
        finally {
            util.close();
        }
    }

}
