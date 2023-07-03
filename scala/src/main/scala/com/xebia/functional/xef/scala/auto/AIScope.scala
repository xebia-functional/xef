package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.auto.CoreAIScope

final case class AIScope(kt: CoreAIScope)
private object AIScope:
  def fromCore(coreAIScope: CoreAIScope): AIScope = new AIScope(coreAIScope)
