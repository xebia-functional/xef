package com.xebia.functional.xef.java.auto.sql;

import com.xebia.functional.xef.auto.PromptConfiguration;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;
import com.xebia.functional.xef.auto.llm.openai.OpenAIModel;
import com.xebia.functional.xef.java.auto.AIDatabase;
import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.sql.jdbc.JdbcConfig;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DatabaseExample {

    private static final BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
    private static final OpenAIModel MODEL = OpenAI.DEFAULT_CHAT;

    @NotNull
    private static JdbcConfig getJdbcConfig() {
        Map<String, String> env = System.getenv();
        String vendor = env.getOrDefault("XEF_SQL_DB_VENDOR", "mysql");
        String host = env.getOrDefault("XEF_SQL_DB_HOST", "localhost");
        String username = env.getOrDefault("XEF_SQL_DB_USER", "user");
        String password = env.getOrDefault("XEF_SQL_DB_PASSWORD", "password");
        int port = Integer.parseInt(env.getOrDefault("XEF_SQL_DB_PORT", "3306"));
        String database = env.getOrDefault("XEF_SQL_DB_DATABASE", "database");
        OpenAIModel model = MODEL;

        JdbcConfig jdbcConfig = new JdbcConfig(vendor, host, username, password, port, database, model);
        return jdbcConfig;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        try (AIScope scope = new AIScope()) {
            AIDatabase database = new AIDatabase(scope, getJdbcConfig());
            System.out.println("llmdb> Welcome to the LLMDB (An LLM interface to your SQL Database) !");
            System.out.println("llmdb> You can ask me questions about the database and I will try to answer them.");
            System.out.println("llmdb> You can type `exit` to exit the program.");
            System.out.println("llmdb> Loading recommended prompts...");

            System.out.println(database.getInterestingPromptsForDatabase().get());

            while(true){
                System.out.println("user> ");
                String input = readLine();
                if (input.equals("exit")) break;

                try {
                    //TODO: Fix this, add docsInContext(50)
                    CompletableFuture<String> result = scope.promptMessage(MODEL, "|\n" +
                            "                You are a database assistant that helps users to query and summarize results from the database.\n" +
                            "                Instructions:\n" +
                            "                1. Summarize the information provided in the `Context` and follow to step 2.\n" +
                            "                2. If the information relates to the `input` then answer the question otherwise return just the summary.\n" +
                            "                ```input\n" +
                            "                " + input + " \n" +
                            "                ```\n" +
                            "                3. Try to answer and provide information with as much detail as you can\n" +
                            "              ", PromptConfiguration.DEFAULTS);
                    for (char c : String.valueOf(result.get()).toCharArray()) {
                        System.out.println("llmdb> " + c);
                    }
                }
                catch (Exception e){
                    System.out.println("llmdb> " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

    }

    private static String readLine() {
        try {
            return sysin.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}
