package com.xebia.functional.xef.java.auto.jdk8.reasoning;

import com.xebia.functional.xef.auto.CoreAIScope;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;
import com.xebia.functional.xef.auto.llm.openai.OpenAIEmbeddings;
import com.xebia.functional.xef.reasoning.filesystem.Files;
import com.xebia.functional.xef.reasoning.pdf.PDF;
import com.xebia.functional.xef.reasoning.text.Text;
import com.xebia.functional.xef.reasoning.tools.ToolSelection;
import java.util.Collections;
import java.util.List;

public class ToolSelectionExample {

    public static void main(String[] args) {
        try (var scope = new CoreAIScope(new OpenAIEmbeddings(OpenAI.DEFAULT_EMBEDDING))) {
            var model = OpenAI.DEFAULT_CHAT;
            var serialization = OpenAI.DEFAULT_SERIALIZATION;
            var text = Text.create(model, scope);
            var files = Files.create(serialization, scope, Collections.emptyList());
            var pdf = PDF.create(model, serialization, scope);

            var toolSelection = new ToolSelection(
                    serialization,
                    scope,
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

