package com.xebia.functional.xef.reasoning.text.events

import com.xebia.functional.xef.auto.Description
import kotlinx.serialization.Serializable

@Serializable
data class Event(
  @Description(["`who`` did `what` to `toWhom` in `where` at `when`"])
  val who: String,
  @Description(["Detailed sentence description of `what` did `who` do to `toWhom` in `where` at `when`"])
  val what: String,
  @Description(["`toWhom` was `what` done to by `who` in `where` at `when`"])
  val toWhom: String,
  @Description(["`when` did `who` do `what` to `toWhom` in `where`"])
  val `when`: String,
  @Description(["`where` did `who` do `what` to `toWhom` at `when`"])
  val where: String,
)
