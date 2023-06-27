package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.auto.CoreAIScope as KtAIScope

final case class AIScope(kt: KtAIScope)
private object AIScope:
  def fromCore(coreAIScope: KtAIScope): AIScope = new AIScope(coreAIScope)
