package com.xebia.functional.xef.auto.tot

// Function to perform solution validation
internal fun <A> checkSolution(response: Solution<A>): Solution<A> {
  println("✅ Validating solution: ${truncateText(response.answer)}...")
  return if (response.isValid) response
  else Solution(response.answer, false, "Invalid solution", null)
}
