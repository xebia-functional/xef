package com.xebia.functional.xef.reasoning.text.arguments

import com.xebia.functional.xef.auto.Description
import kotlinx.serialization.Serializable

@Serializable
data class Argument(
  @Description(["The claim or assertion in an argument"]) val claim: String,
  @Description(["The evidence or reasons supporting the claim"]) val supports: List<String>,
  @Description(["The objections or counterarguments against the claim"])
  val objections: List<String>,
)
