package com.xebia.functional.xef.java.auto;

import com.xebia.functional.xef.auto.CoreAIScope;
import com.xebia.functional.xef.sql.SQL;
import com.xebia.functional.xef.sql.jdbc.JdbcConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AIDatabase {
    private final CoreAIScope scope;
    private SQL sql;
    private SharedExecution exec;

    public AIDatabase(JdbcConfig jdbcConfig, SharedExecution sharedExecution) {
        sql = SQL.Companion.fromJdbcConfigSync(jdbcConfig);
        this.exec = sharedExecution;
        this.scope = sharedExecution.getCoreScope();
    }

    public CompletableFuture<String> getInterestingPromptsForDatabase() {
        return exec.future(continuation -> sql.getInterestingPromptsForDatabase(scope, continuation));
    }

    public void extendContext(List<String> textsToAdd) {
        String[] textsArray = textsToAdd.toArray(new String[textsToAdd.size()]);
        exec.future(continuation -> scope.extendContext(textsArray, continuation));
    }

    public CompletableFuture<List<String>> promptQuery(String input) {
        return exec.future(continuation -> sql.promptQuery(scope, input, continuation));
    }

}
