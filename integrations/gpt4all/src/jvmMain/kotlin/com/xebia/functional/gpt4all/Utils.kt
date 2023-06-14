package com.xebia.functional.gpt4all

import com.sun.jna.Library
import com.sun.jna.Native
import com.xebia.functional.gpt4all.llama.libraries.LlamaLibrary
import java.nio.file.Path

internal fun loadLlamaLibrary(): LlamaLibrary =
    load<LlamaLibrary>(from = "llama")

internal inline fun <reified T : Library> loadLLModelLibrary(): T =
    load<T>(from = "llmodel")

internal inline fun <reified T : Library> load(from: String): T = Native.load(from, T::class.java)

internal fun Path.getModelName(): String =
    toFile().name.split(
        "\\.(?=[^.]+$)".toRegex()
    ).dropLastWhile { it.isEmpty() }.toList()[0]
