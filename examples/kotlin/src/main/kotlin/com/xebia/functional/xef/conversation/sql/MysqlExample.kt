package com.xebia.functional.xef.conversation.sql

import arrow.continuations.SuspendApp
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.sql.SQL
import com.xebia.functional.xef.sql.jdbc.JdbcConfig

object MysqlExample {
    private val mysqlConfig = JdbcConfig(
        vendor = "mysql",
        host = "localhost",
        username = "root",
        password = "toor",
        port = 3307,
        database = "example_db",
        model = OpenAI().DEFAULT_SERIALIZATION
    )

    private val context = """
        Our database is about a small application for selling second-hand products from user to user.
        We operate only in this countries (United States, United Kingdom, Germany, France, Spain). 
        These are the categories that we support in our application
        - Electronics, Clothing, Home & Garden, Sports & Outdoors, Books & Media, Toys & Games, Health & Beauty, Automotive, Furniture, Jewelry & Watches

        Users features and information:
         - Name and lastname
         - Email and the password
         - Birthdate to calculate the age and know if they are of legal age.
         - Are active or not
         - City and country identifier.
         - With the creation and update date.
         
        City and country information:
         - name of the city or country
         - With the creation and update date.

        Product features and information:
        - Name of the product
        - Description of the product
        - Category identifier
        - Price of the product
        - Belongs to only one user at a time.
        - It can only be sold once, and to a single buyer.
        - If the product is sold, it is marked as is_sold to true

        Categories information:
        - The name of each category
        - With the creation and update date.

        Transactions information:
        - The product, seller and buyer identifiers
        - The price that was sold (in our example it is always the same price that appears in products table)
        - The sale date appears as created_at

        Product ratings information:
        - The product identifier
        - The buyer identifier is the user_id who has made the rating
        - The rating that can range from 1 to 5 (where 1 is Bad, 2 Regular, 3 Good, 4 Notable, 5 Excellent)
        - The date of creation of the rating.
        
        Relations between tables:
        - users and cities is 1 to 1.
        - users and countries is 1 to 1.
        - products and categories is 1 to 1.
        - users and products is 1 to N.
        - categories and products is 1 to N.
        - products and transactions is 1 to 1.
        - products and product_ratings is 1 to N.
        - users and transactions is 1 to N.
        - users and product_ratings is 1 to N.
        - cities and users is 1 to N.
        - countries and users is 1 to N.
        - product_ratings and products is 1 to N.
    """.trimIndent()

    @JvmStatic
    fun main(args: Array<String>) = SuspendApp {
        OpenAI.conversation {
            SQL.fromJdbcConfig(mysqlConfig) {
                println(promptQuery("How many tables I have?", listOf(), context))
                println(promptQuery("What kind of questions I can ask you?", listOf(), context))

                println(promptQuery("How many users are active?", listOf("users"), context))

                println(promptQuery("How many users live in Murcia?", listOf("users", "cities"), context))
                println(promptQuery("How many users live in the United States?", listOf("users", "countries"), context))

                println(promptQuery(
                    "Give me the transactions where the product has an excellent rating and has been sold in Spain",
                    listOf("users", "countries", "transactions", "product_ratings"),
                    context
                ))

                println(promptQuery("""Give me the Electronics transaction of the product that 
                |has the highest price that has been sold in France in the city of Lyon,
                |and also that the rating is better than Good.
                |""".trimMargin(),
                    listOf("products", "users", "countries", "cities", "transactions", "product_ratings", "categories"),
                    context
                ))
            }
        }
    }
}

