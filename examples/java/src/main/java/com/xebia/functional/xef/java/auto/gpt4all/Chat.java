package com.xebia.functional.xef.java.auto.gpt4all;

import com.xebia.functional.xef.java.auto.AIScope;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

public class Chat {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var userDir = System.getProperty("user.dir");
        var path = userDir + "/models/gpt4all/ggml-replit-code-v1-3b.bin";

        //var supportedModels = new Gpt4AllModel.supportedModels();

        var url = "https://huggingface.co/nomic-ai/ggml-replit-code-v1-3b/resolve/main/ggml-replit-code-v1-3b.bin";
        var modelPath= Path.of(path);
        //var GPT4All = new GPT4All(url, modelPath);

        try (AIScope scope = new AIScope()) {
        }
    }
}

   /* companion object {
private val url = "https://raw.githubusercontent.com/nomic-ai/gpt4all/main/gpt4all-chat/metadata/models.json"
      fun supportedModels(): List<Gpt4AllModel> {
      // fetch the content as string from https://raw.githubusercontent.com/nomic-ai/gpt4all/main/gpt4all-chat/metadata/models.json
      val json = URL(url).readText()
      // parse the json string into a list of Model objects
      return Json{ ignoreUnknownKeys = true }.decodeFromString<List<Gpt4AllModel>>(json)
      }
      }*/
