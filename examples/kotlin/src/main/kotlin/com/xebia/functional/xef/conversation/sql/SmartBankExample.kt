package com.xebia.functional.xef.conversation.sql

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.sql.QueryPrompter
import com.xebia.functional.xef.sql.jdbc.JdbcConfig

val postgres = JdbcConfig(
    vendor = System.getenv("XEF_SQL_DB_VENDOR") ?: "postgresql",
    host = System.getenv("XEF_SQL_DB_HOST") ?: "localhost",
    username = System.getenv("XEF_SQL_DB_USER") ?: "admin",
    password = System.getenv("XEF_SQL_DB_PASSWORD") ?: "admin",
    port = System.getenv("XEF_SQL_DB_PORT")?.toInt() ?: 5432,
    database = System.getenv("XEF_SQL_DB_DATABASE") ?: "capidata",
    model = OpenAI().DEFAULT_SERIALIZATION
)

val context = """
  These are the existing values for some fields:
        - The field "amount" is the amount of the transaction. If it is a credit transaction, the amount has a negative value. Keep that into account when the input ask for expenses, or for the most expensive debit transactions.
        - The field "type" can have only two possible values: Credit or Debit
        - The field "category1" can have one of these values: VEHICLE RUNNING EXPENSES, MAINTENANCE, ENTERTAINMENT/SHOPPING/LIFESTYLE, BUSINESS PL, LOAN, UTILITIES, RENT, CASH DEPOSIT, INSURANCE, LIQUOR, EDUCATION, DINING, INVESTMENT, LOGISTICS, CASH WITHDRAWALS, CREDIT CARD PAYMENTS, PERSONAL TRANSFERS, DOWNPAYMENTS, FEE, BANK CHARGES, BROKERAGE, INCOME/EXPENSE
        - The field "category2" can have one of these values: MISC INVESTMENT, LOAN:DISBURSEMENT, GASOLINE OR FUEL, GROCERIES, AUTO INSURANCE, COFFEE SHOPS, CASH WITHDRAWALS, PUBS/BREWERIES/LIQUOR SHOPS, CC FEE:CARD_REPLACEMENT_FEES_REV, ON DEMAND DELIVERY, BROKERAGE, MUTUAL FUND, AIRPORT LOUNGES, GAS AND PIPELINE, MUNICIPAL AND GOVT PAYMENTS, GAMING, LOAN:REPAYMENT, MISC DOWNPAYMENTS, AIRLINES AND TRAVEL, MISC RENT, MISC SPENDS, MISC FEE, GENERAL INSURANCE, CAR RENT, TAXES, ELECTRICITY, MEDICAL OR HEALTH INSURANCE, AUTO MAINTENANCE EXPENSES, BILLS AND UTILITIES, CC FEE:CARD_REPLACEMENT_FEES, DINING MISC, GIFTS, SALARY, CASH DEPOSIT, MISC EDUCATION, LOGISTICS, RECOVERY OR REVERSAL, SALES, RESTAURANTS/DINING/ FAST FOODS/ CAFES, FOOD DELIVERY, MOBILE AND TELEPHONY, PERSONAL TRANSFERS, BANK CHARGES, CLOTHING SHOES ACCESSORIES, ELECTRONICS AND DIGITAL, SERVICE TAX REV, ADVERTISING, CABLE OR SATELLITE SERVICES, INCOME TAX, TOLL, CONTENT STREAMING, LIFE INSURANCE, BONUS, CREDIT CARD PAYMENTS, MISC INCOME OR EXPENSE, MISC MAINTENANCE, HOTELS AND HOSPITALITY, MOVIE AND ENTERTAINMENT, PROPERTY TAX, MISC SPENDS:REVERSAL, PERSONAL CARE AND COSMETICS
        - The field "channel" can have one out of these six value: Cash, SWIFTTransfer, CreditCardRepayment, IntraBankTransfer, PoS, Online
        - The field "city" can be whatever city in the world
        - The field "carbon" is the kilograms of CO2e footprint. If the input is interested in carbon footprint or pollutants transaction, make sure you only include transactions where carbon is not null
""".trimIndent()

suspend fun main() = OpenAI.conversation {
    QueryPrompter.fromJdbcConfig(postgres) {
//        println(promptQuery("I want to know witch category is the most expensive", listOf("transaction"), ""))
        println(promptQuery("Which is the month I have spent the most", listOf("transaction"), ""))
//        println(promptQuery("the 5 most expensive transactions", listOf("transaction", "user"), ""))
//        println(promptQuery("How much I spend in cinema?", listOf("transaction", "user"), context))
//        println(promptQuery("did I get any refund from MixedMart in the last year?", listOf("transaction", "user"), context))
//        println(promptQuery("How many users are born in may", listOf("transaction", "user"), ""))
    }
}
