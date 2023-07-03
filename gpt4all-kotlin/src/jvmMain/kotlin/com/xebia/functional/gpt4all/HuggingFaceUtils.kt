package com.xebia.functional.gpt4all

fun huggingFaceUrl(name: String, artifact:String, extension: String): String =
  "https://huggingface.co/$name/$artifact/resolve/main/$artifact.$extension"
