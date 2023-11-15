package com.xebia.functional.openai.models.ext.edit.create

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CreateEditRequestModel(val value: String) {
  @SerialName(value = "text-davinci-edit-001") `text_davinci_edit_001`("text-davinci-edit-001"),
  @SerialName(value = "code-davinci-edit-001") `code_davinci_edit_001`("code-davinci-edit-001")
}
