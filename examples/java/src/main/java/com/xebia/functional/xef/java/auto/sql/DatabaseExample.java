package com.xebia.functional.xef.java.auto.sql;

import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.sql.jdbc.JdbcConfig;
import com.xebia.functional.tokenizer.ModelType;

public class DatabaseExample {
    private static String getenvOrElse(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null ? defaultValue : value;
    }

    public static void main(String[] args) {
        JdbcConfig config = new JdbcConfig(
                getenvOrElse("XEF_SQL_DB_VENDOR", "mysql"),
                getenvOrElse("XEF_SQL_DB_HOST", "localhost"),
                getenvOrElse("XEF_SQL_DB_USER", "user"),
                getenvOrElse("XEF_SQL_DB_PASSWORD", "password"),
                Integer.parseInt(getenvOrElse("XEF_SQL_DB_PORT", "3306")),
                getenvOrElse("XEF_SQL_DB_DATABASE", "database"),
                ModelType.GPT_3_5_TURBO
        );

        try (AIScope scope = new AIScope()) {
            System.out.println("llmdb> Welcome to the LLMDB (An LLM interface to your SQL Database) !");
            System.out.println("llmdb> You can ask me questions about the database and I will try to answer them.");
            System.out.println("llmdb> You can type `exit` to exit the program.");
            System.out.println("llmdb> Loading recommended prompts...");
        }
    }
}
