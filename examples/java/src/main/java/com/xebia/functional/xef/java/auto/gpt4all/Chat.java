package com.xebia.functional.xef.java.auto.gpt4all;

import com.xebia.functional.gpt4all.GPT4All;
import com.xebia.functional.gpt4all.Gpt4AllModel;
import com.xebia.functional.xef.auto.PromptConfiguration;
import com.xebia.functional.xef.java.auto.AIScope;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class Chat {
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        var userDir = System.getProperty("user.dir");
        var path = userDir + "/models/gpt4all/ggml-replit-code-v1-3b.bin";

        var supportedModels = Gpt4AllModel.Companion.getSupportedModels();

        supportedModels.forEach(it -> {
            var url = (Objects.nonNull(it.getUrl())) ? " - " + it.getUrl() : "";
            System.out.println("🤖 " + it.getName() + url);
        });

        var url = "https://huggingface.co/nomic-ai/ggml-replit-code-v1-3b/resolve/main/ggml-replit-code-v1-3b.bin";
        var modelPath = Path.of(path);
        var gpt4all = GPT4All.Companion.invoke(url, modelPath);

        System.out.println("🤖 GPT4All loaded: " + gpt4all);
        /**
         * Uses internally [HuggingFaceLocalEmbeddings] default of "sentence-transformers", "msmarco-distilbert-dot-v5"
         * to provide embeddings for docs in contextScope.
         */

        try (AIScope scope = new AIScope()) {
            System.out.println("🤖 Context loaded: " + scope.getContext());

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            while(true){
                System.out.println("\n🤖 Enter your question: ");

                String line = br.readLine();
                if (line == null || line.isBlank()) {
                    break;
                }else{
                    var promptConfiguration = new PromptConfiguration.Companion.Builder().docsInContext(2).streamToStandardOut(true).build();
                    List<String> answer = scope.promptStreaming(gpt4all, line, promptConfiguration).get();

                    answer.forEach(it -> {
                        System.out.print(it);
                    });
                }
            }
        }
    }
}
