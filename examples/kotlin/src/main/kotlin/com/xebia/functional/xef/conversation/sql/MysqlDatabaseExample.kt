package com.xebia.functional.xef.conversation.sql

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import org.jetbrains.exposed.sql.Database
import com.xebia.functional.xef.sql.SQL

suspend fun main() {
    val host = "localhost"
    val port = "3307"
    val database = "chinook"
    val dbUser = "root"
    val dbPassword = "toor"
    Database.connect("jdbc:mysql://$host:$port/$database", driver = "com.mysql.cj.jdbc.Driver", user = dbUser, password = dbPassword)

    OpenAI.conversation {
        promptQuery("How are you?", listOf("transaction"), "")
        promptQuery("What is a SQL query?", listOf("transaction"), "")
//        SQL.fromJdbcConfig(postgres) {
//            promptQuery("How are you?", listOf("transaction"), context)
//            promptQuery("What is a SQL query?", listOf("transaction"), context)
//            promptQuery("What kind of questions I can ask you?", listOf("transaction"), context)
//            promptQuery("fail", listOf(), "")
//            promptQuery("The category MAINTENANCE exists?", listOf("transaction"), context)
//            promptQuery("I want to know witch category is the most expensive", listOf("transaction"), context)
//            promptQuery("Show me the 10 categories which I spent more money", listOf("transaction"), context)
//            promptQuery(
//                "Which is the month I have spent the most?",
//                listOf("transaction"),
//                context
//            )
//            promptQuery("the 5 most expensive transactions", listOf("transaction", "user"), context)
//            promptQuery("How much I spend in cinema?", listOf("transaction", "user"), context)
//            println("hi")
//        }
    }
}
