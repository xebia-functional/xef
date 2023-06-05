package com.xebia.functional.gpt4all

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import java.nio.file.Path

sealed interface GPT4AllModel : AutoCloseable {
    val adapter: LLModelAdapter
    val model: Pointer?
    val name: String

    enum class Type {
        LLAMA,
        GPTJ
    }

    fun loadModel(): Boolean

    fun type(): Type =
        when (this) {
            is LlamaModel -> Type.LLAMA
            is GPTJModel -> Type.GPTJ
        }
}

interface LlamaModel : GPT4AllModel {
    companion object {
        operator fun invoke(
            path: Path
        ): LlamaModel = object : LlamaModel {
            override val adapter: LLModelAdapter.Llama = loadAdapter()
            override val model: Pointer? = adapter.llmodel_llama_create()
            override val name: String = path.getModelName()
            override fun loadModel(): Boolean = adapter.llmodel_loadModel(model, path.toString())
            override fun close(): Unit = adapter.llmodel_llama_destroy(model)
        }
    }
}

interface GPTJModel : GPT4AllModel {
    companion object {
        operator fun invoke(
            path: Path
        ): GPTJModel = object : GPTJModel {
            override val adapter: LLModelAdapter.GPTJ = loadAdapter()
            override val model: Pointer? = adapter.llmodel_gptj_create()
            override val name: String = path.getModelName()
            override fun loadModel(): Boolean = adapter.llmodel_loadModel(model, path.toString())
            override fun close(): Unit = adapter.llmodel_gptj_destroy(model)
        }
    }
}

private inline fun <reified T : Library> loadAdapter(): T {
    load<LlamaAdapter>(from = "llama")
    return load<T>(from = "llmodel")
}

private fun Path.getModelName(): String =
    toFile().name.split(
        "\\.(?=[^.]+$)".toRegex()
    ).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

private inline fun <reified T : Library> load(from: String): T = Native.load(from, T::class.java)
