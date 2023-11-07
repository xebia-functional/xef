# SQL Integration

## Overview

The SQL module facilitates SQL query generation and execution. This documentation will guide you through the installation and usage of this module.

## Installation

Ensure that you include the SQL module in your project. You can do so by adding the following dependencies to your project's build configuration:

```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.xebia.functional.xef.sql:sql-module:<version>")
}
```
Make sure to replace <version> with the latest version of the SQL module available on Maven Central.

## Supported Databases

The SQL module is built upon the Exposed framework, which means it is compatible with a variety of relational databases. Below is a list of some common databases that are compatible with the SQL Module:

1. MySQL
2. PostgreSQL
3. SQLite
4. Microsoft SQL Server
5. H2 Database
6. Oracle Database
7. MariaDB

Ensure you have the corresponding database driver library added to your project's dependencies. For example, if you plan to use PostgreSQL, include the PostgreSQL JDBC driver in your project's build configuration

## Getting Started
To get started with the SQL module, you must initialize it with your database configuration.

```kotlin
import com.xebia.functional.xef.sql.SQL
import com.xebia.functional.xef.sql.jdbc.JdbcConfig

JdbcConfig(
    vendor = "postgresql",
    host = "localhost",
    username = "test",
    password = "test",
    port = 5432,
    database = "database",
    model = OpenAI().DEFAULT_SERIALIZATION
)

```

## Usage
Once the configuration is set, you can generate and execute SQL queries based on user prompts and contextual information using the primary function promptQuery. It returns an AnswerResponse object, which includes query results and a friendly sentence.

```kotlin
import com.xebia.functional.xef.sql.QueryResult
import com.xebia.functional.xef.sql.AnswerResponse

val prompt = "How much I spend in cinema?"
val tableNames = listOf("transaction")
val context = """

"""

OpenAI.conversation {
    SQL.fromJdbcConfig(jdbcConfig) {
        val answer = promptQuery(prompt, tableNames, context)
    }
}

```

# Example
Suppose you have a table named "sales" with the following structure:
```roomsql
CREATE TABLE sales (
    id INTEGER PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    category VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    customer_name VARCHAR(255) NOT NULL
);
```

```kotlin
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.sql.SQL

fun main() {
    val postgres = JdbcConfig(
        vendor = "postgresql",
        host = "localhost",
        username = "admin",
        password = "admin",
        port = 5432,
        database = "electronics_sales",
        model = OpenAI().DEFAULT_SERIALIZATION
    )

    val context = """
        These are the existing values for some fields:
        - The field "amount" represents the total sales amount in USD.
        - The field "product_name" contains the name of the product, and examples include "Smartphone", "Laptop", "Tablet", etc.
        - The field "quantity" is the quantity of products sold, expressed as an integer (e.g., 5, 10, 20).
        - The field "category" categorizes the products, and possible categories include "Electronics", "Accessories", "Appliances", etc.
        - The field "date" contains the date of the sale in the format "YYYY-MM-DD".
        - The field "customer_name" represents the name of the customer who made the purchase.
    """.trimIndent()

    Conversation {
        SQL.fromJdbcConfig(postgres) {
            val answer1 = promptQuery("Show me all the sales made today", listOf("sales"), context)
            val answer2 = promptQuery("How much money has been earned in sales this month?", listOf("sales"), context)
        }
    }
}

```

```kotlin
answer1 = AnswerResponse(
    input = "Show me all the sales made today",
    answer = "Here are the sales made today:",
    mainQuery = "SELECT * FROM sales WHERE date = '2023-11-03'",
    detailedQuery = null,
    mainTable = QueryResult(
        columns = listOf(
            Column("id", "INTEGER"),
            Column("product_name", "VARCHAR(255)"),
            Column("quantity", "INTEGER"),
            Column("amount", "NUMERIC(10, 2)"),
            Column("category", "VARCHAR(255)"),
            Column("date", "DATE"),
            Column("customer_name", "VARCHAR(255)")
        ),
        rows = listOf(
            listOf("1", "Smartphone", "5", "150.00", "Electronics", "2023-11-03", "John Doe"),
            listOf("2", "Laptop", "3", "75.00", "Electronics", "2023-11-03", "Jane Smith"),
            listOf("3", "Tablet", "2", "30.00", "Electronics", "2023-11-03", "Bob Johnson")
        )
    ),
    detailedTable = null
)
```

```kotlin
answer2 = AnswerResponse(
    input = "How much money has been earned in sales this month?",
    answer = "The total earnings for this month are 5,000.00.",
    mainQuery = "SELECT SUM(amount) as total_earnings FROM sales WHERE EXTRACT(MONTH FROM date) = EXTRACT(MONTH FROM current_date)",
    detailedQuery = null,
    mainTable = QueryResult(
        columns = listOf(
            Column("total_earnings", "NUMERIC(10, 2)")
        ),
        rows = listOf(
            listOf("5000.00")
        )
    ),
    detailedTable = null
)
```

