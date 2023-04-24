package com.xebia.functional.agents.models

final case class AgentAction(
    tool: String,
    toolInput: Map[String, String] | String,
    log: String
)
