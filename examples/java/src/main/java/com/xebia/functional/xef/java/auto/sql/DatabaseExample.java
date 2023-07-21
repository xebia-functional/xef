package com.xebia.functional.xef.java.auto.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xebia.functional.xef.auto.PromptConfiguration;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;
import com.xebia.functional.xef.auto.llm.openai.OpenAIModel;
import com.xebia.functional.xef.java.auto.AIDatabase;
import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.java.auto.SharedExecution;
import com.xebia.functional.xef.java.auto.util.Util;
import com.xebia.functional.xef.sql.jdbc.JdbcConfig;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DatabaseExample {

    private static final OpenAIModel MODEL = OpenAI.DEFAULT_CHAT;
    private static PrintStream out = System.out;

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

        return new JdbcConfig(vendor, host, username, password, port, database, model);
    }

    static final Function1<? super PromptConfiguration.Companion.Builder, Unit> promptConfiguration =
            (Function1<PromptConfiguration.Companion.Builder, Unit>) builder -> {
                builder.docsInContext(50);
                return Unit.INSTANCE;
            };

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        SharedExecution sharedExecution = new SharedExecution();
        try (AIScope scope = new AIScope(new ObjectMapper(), sharedExecution)) {
            AIDatabase database = new AIDatabase(getJdbcConfig(), sharedExecution);

            out.println("llmdb> Welcome to the LLMDB (An LLM interface to your SQL Database) !");
            out.println("llmdb> You can ask me questions about the database and I will try to answer them.");
            out.println("llmdb> You can type `exit` to exit the program.");
            out.println("llmdb> Loading recommended prompts...");

            Arrays.stream(database.getInterestingPromptsForDatabase().get()
                    .split("\n")).forEach(it -> out.println("llmdb> " + it));

            while (true) {
                out.println("user> ");
                String input = Util.readLine();
                if (input.equals("exit")) break;

                try {
                    database.extendContext(database.promptQuery(input).get());
                    CompletableFuture<String> result = scope.promptMessage(MODEL, "|\n" +
                            "                You are a database assistant that helps users to query and summarize results from the database.\n" +
                            "                Instructions:\n" +
                            "                1. Summarize the information provided in the `Context` and follow to step 2.\n" +
                            "                2. If the information relates to the `input` then answer the question otherwise return just the summary.\n" +
                            "                ```input\n" +
                            "                " + input + " \n" +
                            "                ```\n" +
                            "                3. Try to answer and provide information with as much detail as you can\n" +
                            "              ", PromptConfiguration.Companion.build(promptConfiguration));

                    out.println(result.get());
                } catch (Exception e) {
                    out.println("llmdb> " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

    }


}
