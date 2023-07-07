package com.xebia.functional.xef.java.auto.gpt4all;

import com.xebia.functional.xef.java.auto.AIScope;
import java.util.concurrent.ExecutionException;

public class Chat {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var userDir = System.getProperty("user.dir");
        var path = userDir + "/models/gpt4all/ggml-replit-code-v1-3b.bin";

        //var supportedModels = Gpt4AllModel.supportedModels();

        try (AIScope scope = new AIScope()) {
        }
    }
}
