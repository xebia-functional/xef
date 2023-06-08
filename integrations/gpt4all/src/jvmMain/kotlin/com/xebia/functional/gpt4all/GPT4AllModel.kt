package com.xebia.functional.gpt4all

import com.xebia.functional.gpt4all.libraries.LLModelContext
import java.nio.file.Path

interface GPT4AllModel : AutoCloseable {
    val llModel: LLModel

    fun prompt(prompt: String, context: LLModelContext): String
    fun embeddings(prompt: String): List<Float>

    companion object {
        operator fun invoke(
            path: Path,
            modelType: LLModel.Type
        ): GPT4AllModel = object : GPT4AllModel {
            override val llModel: LLModel =
                when (modelType) {
                    LLModel.Type.LLAMA -> LlamaModel(path)
                    LLModel.Type.GPTJ -> GPTJModel(path)
                    LLModel.Type.MPT -> MPTModel(path)
                }.also { it.loadModel() }

            override fun prompt(prompt: String, context: LLModelContext): String =
                with(llModel) {
                    val responseBuffer = StringBuffer()
                    llModelLibrary.llmodel_prompt(
                        model,
                        prompt,
                        { _, _ -> true },
                        { _, response -> responseBuffer.append(response).let { true } },
                        { _ -> true },
                        context
                    )
                    responseBuffer.trim().toString()
                }

            override fun embeddings(prompt: String): List<Float> = TODO("Not yet implemented")

            override fun close(): Unit = llModel.close()
        }
    }
}
