package ai.xef.openai.generator;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public class DownloadOpenAIAPI {
    public static void main(String[] args) {
        try {
            String commit = readCommit();
            downloadAPI(commit);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String readCommit() throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader("config/openai-api-commit"))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString().trim();
    }

    private static void downloadAPI(String commit) throws IOException {
        URL url = new URL("https://raw.githubusercontent.com/openai/openai-openapi/%s/openapi.yaml".formatted(commit));
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        try (FileOutputStream fileOutputStream = new FileOutputStream("config/openai-api.yaml")) {
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }
    }
}
