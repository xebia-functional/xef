package com.xebia.functional.xef.scala.agents

import com.xebia.functional.xef.agents.Search
import com.xebia.functional.loom.LoomAdapter
import scala.jdk.CollectionConverters.*

object DefaultSearch:
  def search(prompt: String): List[String] =
    LoomAdapter.apply[java.util.List[String]](Search.search(prompt, _)).asScala.toList
