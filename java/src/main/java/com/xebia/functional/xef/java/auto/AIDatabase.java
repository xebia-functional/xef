package com.xebia.functional.xef.java.auto;

import com.xebia.functional.xef.sql.SQL;
import com.xebia.functional.xef.sql.jdbc.JdbcConfig;

import java.util.concurrent.CompletableFuture;

public class AIDatabase {
    private AIScope scope;
    private JdbcConfig jdbcConfig;
    private SQL sql;

    public AIDatabase(AIScope scope, JdbcConfig jdbcConfig) {
        this.scope = scope;
        this.jdbcConfig = jdbcConfig;
        sql = SQL.Companion.fromJdbcConfigSync(jdbcConfig);
    }

    public CompletableFuture<String> getInterestingPromptsForDatabase() {

        return scope.getInterestingPromptsForDatabase(sql);
    }


    public Object promptMessage(String question, Object context, Object o1, Object o2, Object o3) {
        return null;
    }
}
