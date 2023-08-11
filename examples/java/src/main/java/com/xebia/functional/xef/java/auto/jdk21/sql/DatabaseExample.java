package com.xebia.functional.xef.java.auto.jdk21.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xebia.functional.xef.auto.PromptConfiguration;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;
import com.xebia.functional.xef.auto.llm.openai.OpenAIModel;
import com.xebia.functional.xef.java.auto.AIDatabase;
import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.java.auto.ExecutionContext;
import com.xebia.functional.xef.java.auto.jdk21.util.ConsoleUtil;
import com.xebia.functional.xef.sql.jdbc.JdbcConfig;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;

public class DatabaseExample {

    private static final OpenAIModel MODEL = new OpenAI().DEFAULT_CHAT;
    private static PrintStream out = System.out;
    private static ConsoleUtil util = new ConsoleUtil();

    @NotNull
    private static JdbcConfig getJdbcConfig() {
        var env = System.getenv();
        var vendor = env.getOrDefault("XEF_SQL_DB_VENDOR", "mysql");
        var host = env.getOrDefault("XEF_SQL_DB_HOST", "localhost");
        var username = env.getOrDefault("XEF_SQL_DB_USER", "user");
        var password = env.getOrDefault("XEF_SQL_DB_PASSWORD", "password");
        var port = Integer.parseInt(env.getOrDefault("XEF_SQL_DB_PORT", "3306"));
        var database = env.getOrDefault("XEF_SQL_DB_DATABASE", "database");
        var model = MODEL;

        return new JdbcConfig(vendor, host, username, password, port, database, model);
    }

    static final Function1<? super PromptConfiguration.Companion.Builder, Unit> promptConfiguration =
            (Function1<PromptConfiguration.Companion.Builder, Unit>) builder -> {
                builder.docsInContext(50);
                return Unit.INSTANCE;
            };

    public static void main(String[] args) throws Exception {

        var executionContext = new ExecutionContext(Executors.newVirtualThreadPerTaskExecutor());
        try (var scope = new AIScope(new ObjectMapper(), executionContext)) {
            var database = new AIDatabase(getJdbcConfig(), executionContext);

            out.println("llmdb> Welcome to the LLMDB (An LLM interface to your SQL Database) !");
            out.println("llmdb> You can ask me questions about the database and I will try to answer them.");
            out.println("llmdb> You can type `exit` to exit the program.");
            out.println("llmdb> Loading recommended prompts...");

            Arrays.stream(database.getInterestingPromptsForDatabase().get()
                    .split("\n")).forEach(it -> out.println("llmdb> " + it));

            while (true) {
                out.println("user> ");
                var input = util.readLine();
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
        finally {
            util.close();
        }

    }


}
