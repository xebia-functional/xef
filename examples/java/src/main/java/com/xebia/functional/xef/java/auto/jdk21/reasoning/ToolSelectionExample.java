package com.xebia.functional.xef.java.auto.jdk21.reasoning;

import com.xebia.functional.xef.auto.llm.openai.OpenAI;
import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.java.auto.ExecutionContext;
import com.xebia.functional.xef.reasoning.filesystem.Files;
import com.xebia.functional.xef.reasoning.pdf.PDF;
import com.xebia.functional.xef.reasoning.text.Text;
import com.xebia.functional.xef.reasoning.tools.ToolSelection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class ToolSelectionExample {

    public static void main(String[] args) {
        try (var scope = new AIScope(new ExecutionContext(Executors.newVirtualThreadPerTaskExecutor()))) {
            var model = OpenAI.DEFAULT_CHAT;
            var serialization = OpenAI.DEFAULT_SERIALIZATION;
            var text = Text.create(model, scope.getScope());
            var files = Files.create(serialization, scope.getScope(), Collections.emptyList());
            var pdf = PDF.create(model, serialization, scope.getScope());

            var toolSelection = new ToolSelection(
                    serialization,
                    scope.getScope(),
                    List.of(
                            text.summarize,
                            pdf.readPDFFromUrl,
                            files.readFile,
                            files.writeToTextFile
                    ),
                    Collections.emptyList()
            );

            var inputText = "Extract information from https://arxiv.org/pdf/2305.10601.pdf";
            var result = toolSelection.applyInferredToolsBlocking(inputText);
            System.out.println(result);
        }
    }
}

