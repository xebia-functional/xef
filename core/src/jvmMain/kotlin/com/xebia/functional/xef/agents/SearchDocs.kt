package com.xebia.functional.xef.agents

import com.xebia.functional.xef.auto.ExecutionContext
import java.util.concurrent.CompletableFuture

object SearchDocs {
  @JvmStatic
  fun search(prompt: String): CompletableFuture<List<String>> =
    ExecutionContext().future {
      com.xebia.functional.xef.agents.search(prompt)
    }
}
