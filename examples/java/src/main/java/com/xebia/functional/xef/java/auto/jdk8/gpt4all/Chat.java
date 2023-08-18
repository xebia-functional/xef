package com.xebia.functional.xef.java.auto.jdk8.gpt4all;

import com.xebia.functional.gpt4all.GPT4All;
import com.xebia.functional.gpt4all.Gpt4AllModel;
import com.xebia.functional.xef.conversation.PlatformConversation;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import com.xebia.functional.xef.conversation.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class Chat {
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        String userDir = System.getProperty("user.dir");
        String path = userDir + "/models/gpt4all/ggml-replit-code-v1-3b.bin";

        List<Gpt4AllModel> supportedModels = Gpt4AllModel.Companion.getSupportedModels();

        supportedModels.forEach(it -> {
            String url = (Objects.nonNull(it.getUrl())) ? " - " + it.getUrl() : "";
            System.out.println("🤖 " + it.getName() + url);
        });

        String url = "https://huggingface.co/nomic-ai/ggml-replit-code-v1-3b/resolve/main/ggml-replit-code-v1-3b.bin";
        Path modelPath = Path.of(path);
        GPT4All gpt4all = GPT4All.Companion.invoke(url, modelPath);

        System.out.println("🤖 GPT4All loaded: " + gpt4all);
        /**
         * Uses internally [HuggingFaceLocalEmbeddings] default of "sentence-transformers", "msmarco-distilbert-dot-v5"
         * to provide embeddings for docs in contextScope.
         */

        try (PlatformConversation scope = OpenAI.conversation();
             BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("\n🤖 Enter your question: ");

            while(true){
                String line = br.readLine();
                if (line.equals("exit")) break;

                Publisher<String> answer = scope.promptStreamingToPublisher(gpt4all, new Prompt(line));

                answer.subscribe(new Subscriber<String>() {
                    StringBuilder answer = new StringBuilder();

                    @Override
                    public void onSubscribe(Subscription s) {
                        System.out.print("\n🤖 --> " + s);
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(String s) {
                        answer.append(s);
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.out.println(t);
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("\n🤖 --> " + answer.toString());
                        System.out.println("\n🤖 --> Done");
                        System.out.println("\n🤖 Enter your question: ");
                    }
                });
            }
        }
    }
}
