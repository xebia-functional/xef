package com.xebia.functional.gpt4all.llmodel

import com.sun.jna.Pointer
import com.xebia.functional.gpt4all.getModelName
import com.xebia.functional.gpt4all.llama.libraries.LlamaLibrary
import com.xebia.functional.gpt4all.llmodel.libraries.LLModelContextParams
import com.xebia.functional.gpt4all.llmodel.libraries.LLModelLibrary
import com.xebia.functional.gpt4all.loadLLModelLibrary
import com.xebia.functional.gpt4all.loadLlamaLibrary
import java.nio.file.Path

sealed interface GPT4AllModel : AutoCloseable {
    val llamaLibrary: LlamaLibrary
    val llModelLibrary: LLModelLibrary
    val model: Pointer?
    val name: String

    enum class Type {
        LLAMA,
        GPTJ,
        MPT
    }

    fun type(): Type =
        when (this) {
            is LLAMA -> Type.LLAMA
            is GPTJ -> Type.GPTJ
            is MPT -> Type.MPT
        }

    fun prompt(prompt: String, contextParams: LLModelContextParams): String {
        val responseBuffer = StringBuffer()

        llModelLibrary.llmodel_prompt(
            model,
            prompt,
            { _, _ -> true },
            { _, response -> responseBuffer.append(response).let { true } },
            { _ -> true },
            contextParams
        )
        return responseBuffer.trim().toString()
    }

    interface LLAMA : GPT4AllModel {
        companion object {
            operator fun invoke(
                path: Path
            ): LLAMA = object : LLAMA {
                override val llamaLibrary: LlamaLibrary = loadLlamaLibrary()
                override val llModelLibrary: LLModelLibrary.Llama = loadLLModelLibrary()
                override val model: Pointer? = llModelLibrary.loadModel(path)
                override val name: String = path.getModelName()
                override fun close(): Unit = llModelLibrary.llmodel_llama_destroy(model)
            }
        }
    }

    interface GPTJ : GPT4AllModel {
        companion object {
            operator fun invoke(
                path: Path
            ): GPTJ = object : GPTJ {
                override val llamaLibrary: LlamaLibrary = loadLlamaLibrary()
                override val llModelLibrary: LLModelLibrary.GPTJ = loadLLModelLibrary()
                override val model: Pointer? = llModelLibrary.loadModel(path)
                override val name: String = path.getModelName()
                override fun close(): Unit = llModelLibrary.llmodel_gptj_destroy(model)
            }
        }
    }

    interface MPT : GPT4AllModel {
        companion object {
            operator fun invoke(
                path: Path
            ): MPT = object : MPT {
                override val llamaLibrary: LlamaLibrary = loadLlamaLibrary()
                override val llModelLibrary: LLModelLibrary.MPT = loadLLModelLibrary()
                override val model: Pointer? = llModelLibrary.loadModel(path)
                override val name: String = path.getModelName()
                override fun close(): Unit = llModelLibrary.llmodel_mpt_destroy(model)
            }
        }
    }
}

private fun LLModelLibrary.loadModel(path: Path): Pointer? =
    when(this) {
        is LLModelLibrary.Llama -> llmodel_llama_create()
        is LLModelLibrary.GPTJ -> llmodel_gptj_create()
        is LLModelLibrary.MPT -> llmodel_mpt_create()
    }.also { model -> llmodel_loadModel(model, path.toString()) }
