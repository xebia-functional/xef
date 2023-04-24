package com.xebia.functional.agents.models

final case class AgentFinish(
    returnValues: Map[String, String],
    log: String
)
