package com.xebia.functional.gpt4all

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL

@Serializable
data class Gpt4AllModel(
  val order: String,
  val md5sum: String,
  val name: String,
  val filename: String,
  val filesize: String,
  val requires: String? = null,
  val ramrequired: String,
  val parameters: String,
  val quant: String,
  val type: String,
  val description: String,
  val disableGUI: String? = null,
  val url: String? = null,
  val promptTemplate: String? = null,
  val systemPrompt: String? = null,
) {
  companion object {
    private val url = "https://raw.githubusercontent.com/nomic-ai/gpt4all/main/gpt4all-chat/metadata/models.json"
    val supportedModels : List<Gpt4AllModel> by lazy {
      // fetch the content as string from https://raw.githubusercontent.com/nomic-ai/gpt4all/main/gpt4all-chat/metadata/models.json
      val json = URL(url).readText()
      // parse the json string into a list of Model objects
      Json.decodeFromString<List<Gpt4AllModel>>(json)
    }
  }
}
