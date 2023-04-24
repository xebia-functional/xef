package com.xebia.functional.agents.models

final case class LLMAgentHistory(
    step: AgentAction,
    observation: String
)
