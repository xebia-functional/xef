package com.xebia.functional.xef.java.auto.jdk8.reasoning;

import com.xebia.functional.xef.auto.CoreAIScope;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;
import com.xebia.functional.xef.auto.llm.openai.OpenAIEmbeddings;
import com.xebia.functional.xef.auto.llm.openai.OpenAIModel;
import com.xebia.functional.xef.reasoning.filesystem.Files;
import com.xebia.functional.xef.reasoning.pdf.PDF;
import com.xebia.functional.xef.reasoning.text.Text;
import com.xebia.functional.xef.reasoning.tools.ToolSelection;
import java.util.Collections;
import java.util.List;

public class ToolSelectionExample {

    public static void main(String[] args) {
        try (CoreAIScope scope = new CoreAIScope(new OpenAIEmbeddings(OpenAI.DEFAULT_EMBEDDING))) {
            OpenAIModel model = OpenAI.DEFAULT_CHAT;
            OpenAIModel serialization = OpenAI.DEFAULT_SERIALIZATION;
            Text text = Text.create(model, scope);
            Files files = Files.create(serialization, scope, Collections.emptyList());
            PDF pdf = PDF.create(model, serialization, scope);

            ToolSelection toolSelection = new ToolSelection(
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

            String inputText = "Extract information from https://arxiv.org/pdf/2305.10601.pdf";
            var result = toolSelection.applyInferredToolsBlocking(inputText);
            System.out.println(result);
        }
    }
}

