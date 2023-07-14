package com.xebia.functional.xef.java.auto.gpt4all;

import com.xebia.functional.gpt4all.GPT4All;
import com.xebia.functional.xef.auto.PromptConfiguration;
import com.xebia.functional.xef.java.auto.AIScope;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowCollector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//import kotlinx.coroutines.flow.onCompletion;

public class Chat {
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        var userDir = System.getProperty("user.dir");
        var path = userDir + "/models/gpt4all/ggml-replit-code-v1-3b.bin";

        //var supportedModels = new Gpt4AllModel.supportedModels();

        var url = "https://huggingface.co/nomic-ai/ggml-replit-code-v1-3b/resolve/main/ggml-replit-code-v1-3b.bin";
        var modelPath= Path.of(path);
        var gpt4all = GPT4All.Companion.invoke(url, modelPath);

        System.out.println("🤖 GPT4All loaded: " + gpt4all);
        /**
         * Uses internally [HuggingFaceLocalEmbeddings] default of "sentence-transformers", "msmarco-distilbert-dot-v5"
         * to provide embeddings for docs in contextScope.
         */

        try (AIScope scope = new AIScope()) {
            System.out.println("🤖 Context loaded: " + scope.getContext());

            var out = System.out;

            while(true){
                System.out.println("\n🤖 Enter your question: ");
                //var userInput = readlnOrNull() ?: break;

                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String line = br.readLine();
                if (line == null || line.isBlank()) {
                    break;
                }else{
                    var promptConfiguration = PromptConfiguration.Companion.buildWithParams(2, true);
                    Flow<String> aux = scope.promptStreaming(gpt4all, line, scope.getContext(), promptConfiguration).get();
                    /*aux.(new FlowCollector<String>() {
                        @Nullable
                        @Override
                        public Object emit(String s,
                              @NotNull Continuation<? super Unit> continuation) {
                            System.out.println(s);
                            return null;
                        }
                    });*/

                    //var aux2 = gpt4all.promptStreaming(line, scope.getContext(), promptConfiguration);
                }
            }
        }
    }
}
