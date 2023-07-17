package com.xebia.functional.xef.auto.reasoning

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.OpenAIEmbeddings
import com.xebia.functional.xef.reasoning.code.Code
import com.xebia.functional.xef.reasoning.tools.ToolSelection

suspend fun main() {
  val scope = CoreAIScope(OpenAIEmbeddings(OpenAI.DEFAULT_EMBEDDING))
  val chatModel = OpenAI.DEFAULT_CHAT
  val serializationModel = OpenAI.DEFAULT_SERIALIZATION
  val code = Code(chatModel = chatModel, serializationModel = serializationModel, scope = scope)

  val sourceCode = """
       import java.util.*

       class ShoppingCart {
           private val items: MutableList<String> = mutableListOf()

           fun addItem(item: String) {
               items.add(item)
           }

           fun removeItem(item: String) {
               items.remove(item)
           }

           fun getTotalItems(): Int {
               return items.size
           }

           fun calculateTotalPrice(): Double {
               var totalPrice = 0.0
               for (item in items) {
                   val price = fetchItemPrice(item)
                   totalPrice += price
               }
               return totalPrice
           }

           private fun fetchItemPrice(item: String): Double {
               // Logic to fetch item price from database or API
               // For simplicity, return a random price
               return Random().nextDouble() * 100
           }
       }

       fun main() {
           val cart = ShoppingCart()
           cart.addItem("Item 1")
           cart.addItem("Item 2")
           cart.addItem("Item 3")

           println("Total items in cart: ${'$'}{cart.getTotalItems()}")
           println("Total price of items in cart: ${'$'}{cart.calculateTotalPrice()}")

           cart.removeItem("Item 2")

           println("Total items in cart: ${'$'}{cart.getTotalItems()}")
           println("Total price of items in cart: ${'$'}{cart.calculateTotalPrice()}")
       }

    """.trimIndent()

  val toolSelection = ToolSelection(
    serializationModel,
    scope,
    code.tools
  )

  val result = toolSelection.applyFunction<Any>("""|
    |Find bugs in this code:
    |```code
    |$sourceCode
    |```
  """.trimMargin())
  println(result)
}
