package org.example.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class XefRuleSetProvider : RuleSetProvider {
  override val ruleSetId: String = "XefRuleSet"

  override fun instance(config: Config): RuleSet {
    return RuleSet(
      ruleSetId,
      listOf(JvmInlineAnnotation(config), PublicDataClassConstructorWithValueParameters(config)),
    )
  }
}
