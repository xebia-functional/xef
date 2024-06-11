package com.xebia.functional.xef

import com.xebia.functional.xef.conversation.Description
import kotlinx.serialization.Serializable

@Serializable
@Description("The selected items indexes")
data class SelectedItems(
  @Description("The selected items indexes") val selectedItems: List<Int>,
)
