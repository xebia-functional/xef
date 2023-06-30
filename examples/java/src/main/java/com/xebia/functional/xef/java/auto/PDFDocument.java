package com.xebia.functional.xef.java.auto;

import com.xebia.functional.tokenizer.ModelType;
import com.xebia.functional.xef.textsplitters.TextSplitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.xebia.functional.xef.textsplitters.TokenTextSplitterKt.TokenTextSplitter;

public class PDFDocument {

    public static class AIResponse{
        public String answer;
        public String source;
    }
    private static final String pdfUrl = "https://people.cs.ksu.edu/~schmidt/705a/Scala/Programming-in-Scala.pdf";

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        TextSplitter textSplitter = TokenTextSplitter(ModelType.GPT_3_5_TURBO, 100, 50);

        try (AIScope scope = new AIScope()) {
            CompletableFuture<List<String>> pdf = scope.pdf(pdfUrl, textSplitter);
//            scope.contextScope(pdf, ?)

            while(true){
                System.out.println("Enter your question: ");
                String line = System.console().readLine();
                if(line == null) break;
                scope.prompt(line, PDFDocument.AIResponse.class)
                        .thenAccept((aiRes) -> System.out.println(aiRes.answer + "\n---\n" +
                                aiRes.source + "\n---\n"))
                        .get();
            }
        }
    }
}
