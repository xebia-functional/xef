package com.xebia.functional.gpt4all

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.xebia.functional.gpt4all.libraries.LLModelLibrary
import com.xebia.functional.gpt4all.libraries.LlamaLibrary
import java.nio.file.Path

sealed interface LLModel : AutoCloseable {
    val llamaLibrary: LlamaLibrary
    val llModelLibrary: LLModelLibrary
    val model: Pointer?
    val name: String

    enum class Type {
        LLAMA,
        GPTJ,
        MPT
    }

    fun loadModel(): Boolean

    fun type(): Type =
        when (this) {
            is LlamaModel -> Type.LLAMA
            is GPTJModel -> Type.GPTJ
            is MPTModel -> Type.MPT
        }
}

interface LlamaModel : LLModel {
    companion object {
        operator fun invoke(
            path: Path
        ): LlamaModel = object : LlamaModel {
            override val llamaLibrary: LlamaLibrary = loadLlamaLibrary()
            override val llModelLibrary: LLModelLibrary.Llama = loadLLModelLibrary()
            override val model: Pointer? = llModelLibrary.llmodel_llama_create()
            override val name: String = path.getModelName()
            override fun loadModel(): Boolean = llModelLibrary.llmodel_loadModel(model, path.toString())
            override fun close(): Unit = llModelLibrary.llmodel_llama_destroy(model)
        }
    }
}

interface GPTJModel : LLModel {
    companion object {
        operator fun invoke(
            path: Path
        ): GPTJModel = object : GPTJModel {
            override val llamaLibrary: LlamaLibrary = loadLlamaLibrary()
            override val llModelLibrary: LLModelLibrary.GPTJ = loadLLModelLibrary()
            override val model: Pointer? = llModelLibrary.llmodel_gptj_create()
            override val name: String = path.getModelName()
            override fun loadModel(): Boolean = llModelLibrary.llmodel_loadModel(model, path.toString())
            override fun close(): Unit = llModelLibrary.llmodel_gptj_destroy(model)
        }
    }
}

interface MPTModel : LLModel {
    companion object {
        operator fun invoke(
            path: Path
        ): MPTModel = object : MPTModel {
            override val llamaLibrary: LlamaLibrary = loadLlamaLibrary()
            override val llModelLibrary: LLModelLibrary.MPT = loadLLModelLibrary()
            override val model: Pointer? = llModelLibrary.llmodel_mpt_create()
            override val name: String = path.getModelName()
            override fun loadModel(): Boolean = llModelLibrary.llmodel_loadModel(model, path.toString())
            override fun close(): Unit = llModelLibrary.llmodel_mpt_destroy(model)
        }
    }
}

private fun loadLlamaLibrary(): LlamaLibrary =
    load<LlamaLibrary>(from = "llama")

private inline fun <reified T : Library> loadLLModelLibrary(): T =
    load<T>(from = "llmodel")

private fun Path.getModelName(): String =
    toFile().name.split(
        "\\.(?=[^.]+$)".toRegex()
    ).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

private inline fun <reified T : Library> load(from: String): T = Native.load(from, T::class.java)
