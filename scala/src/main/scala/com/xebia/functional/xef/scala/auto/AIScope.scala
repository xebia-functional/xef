package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.auto.Conversation

final case class AIScope(kt: Conversation)
private object AIScope:
  def fromCore(conversation: Conversation): AIScope = new AIScope(conversation)
