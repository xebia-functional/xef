package com.xebia.functional.xef.assistants.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class PackageSearchResponse(
  val help: String,
  val success: Boolean,
  val result: Result,
) {
  @Serializable
  data class Result(
    val count: Int,
    val results: List<Result>,
  ) {


    @Serializable
    data class Result(
      val author: String? = null,
      val author_email: String? = null,
      val creator_user_id: String,
      val id: String,
      val isopen: Boolean,
      val license_id: String,
      val license_title: String,
      val maintainer: String,
      val maintainer_email: String,
      val metadata_created: String,
      val metadata_modified: String,
      val name: String,
      val notes: String,
      val num_resources: Int,
      val num_tags: Int,
      val organization: Organization,
      val owner_org: String,
      val `private`: Boolean,
      val state: String,
      val title: String,
      val type: String,
      val url: String?,
      val version: String?,
      val extras: List<Extra>,
      val resources: List<Resource>,
      val tags: List<Tag>
    ) {
      @Serializable
      data class Organization(
        val id: String,
        val name: String,
        val title: String,
        val type: String,
        val description: String,
        val image_url: String,
        val created: String,
        val is_organization: Boolean,
        val approval_status: String,
        val state: String,
      )

      @Serializable
      data class Extra(
        val key: String,
        val value: JsonElement,
      )

      @Serializable
      data class Resource(
        val cache_last_updated: String? = null,
        val cache_url: String? = null,
        val created: String,
        val description: String,
        val format: String,
        val hash: String,
        val id: String,
        val last_modified: String? = null,
        val metadata_modified: String,
        val mimetype: String,
        val mimetype_inner: String? = null,
        val name: String,
        val no_real_name: Boolean? = null,
        val package_id: String,
        val position: Int,
        val resource_type: String? = null,
        val size: String? = null,
        val state: String,
        val url: String,
        val url_type: String? = null,
      )

      @Serializable
      data class Tag(
        val display_name: String,
        val id: String,
        val name: String,
        val state: String,
        val vocabulary_id: String? = null,
      )
    }
  }
}

