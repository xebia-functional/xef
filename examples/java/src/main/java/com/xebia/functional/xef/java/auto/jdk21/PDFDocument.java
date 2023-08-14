package com.xebia.functional.xef.java.auto.jdk21;

import com.xebia.functional.xef.auto.PlatformConversation;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;
import com.xebia.functional.xef.java.auto.jdk21.util.ConsoleUtil;
import com.xebia.functional.xef.reasoning.pdf.PDF;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PDFDocument {

    private static ConsoleUtil util = new ConsoleUtil();

    public record AIResponse(String answer, String source){}

    private static final String PDF_URL = "https://people.cs.ksu.edu/~schmidt/705a/Scala/Programming-in-Scala.pdf";

    private static CompletableFuture<Void> askQuestion(PlatformConversation scope) {
        System.out.println("Enter your question (<return> to exit): ");


        var line = util.readLine();
        if (line == null || line.isBlank()) {
            return CompletableFuture.completedFuture(null);
        } else {
            scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, line, AIResponse.class)
                    .thenAccept(aiRes -> System.out.println(aiRes.answer + "\n---\n" +
                            aiRes.source + "\n---\n"));

            return askQuestion(scope);
        }
    }

    public static void main(String[] args) throws Exception {
        try (PlatformConversation scope = OpenAI.conversation()) {
            PDF pdf = new PDF(OpenAI.FromEnvironment.DEFAULT_CHAT,
                    OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, scope);
            scope.addContext(List.of(pdf.readPDFFromUrl.readPDFFromUrl(PDF_URL).get()));
            askQuestion(scope).get();
        }
        finally {
            util.close();
        }
    }

}
