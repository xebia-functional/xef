package com.xebia.functional.agents

import com.xebia.functional.auto.agents.Agent
import com.xebia.functional.auto.agents.WikipediaResult
import com.xebia.functional.auto.agents.wikipedia
import com.xebia.functional.io.CommandExecutor
import com.xebia.functional.io.SYSTEM

fun Agent.Companion.wikipedia(): Agent<WikipediaResult> =
  Agent.wikipedia(CommandExecutor.SYSTEM)
