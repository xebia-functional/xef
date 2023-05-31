package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.auto.{AIScope as KtAIScope, Agent as KtAgent}

final case class AIScope(kt: KtAIScope)
private object AIScope:
  def fromCore(coreAIScope: KtAIScope): AIScope = new AIScope(coreAIScope)
