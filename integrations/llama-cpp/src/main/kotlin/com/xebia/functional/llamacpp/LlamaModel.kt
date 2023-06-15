package com.xebia.functional.llamacpp

import com.sun.jna.Memory
import com.sun.jna.Pointer
import com.xebia.functional.llamacpp.libraries.LlamaLibrary
import java.nio.file.Path

interface LlamaModel : AutoCloseable {
    val library: LlamaLibrary
    val context: LlamaContext
    val name: String

    fun embeddings(
        text: String,
        generationConfig: LlamaGenerationConfig = LlamaGenerationConfig()
    ): List<Float>

    fun encode(text: String, addBos: Boolean): List<Int>
    fun decode(tokens: List<Int>): String

    companion object {
        operator fun invoke(
            path: Path
        ): LlamaModel = object : LlamaModel {
            override val library: LlamaLibrary = loadLlamaLibrary()
            override val context: LlamaContext = LlamaContext(library, LlamaConfig(path.toString()))
            override val name: String = path.getModelName()

            override fun embeddings(text: String, generationConfig: LlamaGenerationConfig): List<Float> {
                val tokens: IntArray = getTokensForEmbeddings(text)
                val tokensPointer: Memory = getTokensPointer(tokens)
                library.llama_eval(context.pointer, tokensPointer, tokens.size, 0 , generationConfig.n_threads)
                return library.getEmbeddings()
            }

            override fun encode(text: String, addBos: Boolean): List<Int> {
                val nMaxTokens: Int = text.length + if (addBos) 1 else 0
                val cText: ByteArray = text.toByteArray()
                val textPointer: Memory = Memory(cText.size.toLong())
                    .apply { write(0, cText, 0, cText.size) }
                val tokensPointer = Memory(nMaxTokens * Int.SIZE_BYTES.toLong())
                val n: Int = library.llama_tokenize(context.pointer, textPointer, tokensPointer, nMaxTokens, addBos)
                check(n >= 0)
                return (0 until n).map { tokensPointer.getInt(it * Int.SIZE_BYTES.toLong()) }
            }

            override fun decode(tokens: List<Int>): String =
                tokens.joinToString("") { token ->
                    library.llama_token_to_str(context.pointer, token)
                }

            override fun close(): Unit =
                library.llama_free(context.pointer)

            private fun getTokensForEmbeddings(text: String): IntArray {
                val tokens: List<Int> = encode(text, true)
                val bosToken: Int = library.llama_token_bos()
                val tokensWithBos: List<Int> = listOf(bosToken) + tokens
                return tokensWithBos.toIntArray()
            }

            private fun LlamaLibrary.getEmbeddings(): List<Float> {
                val size: Int = llama_n_embd(context.pointer)
                val pointer: Pointer = llama_get_embeddings(context.pointer)
                val embeddings: FloatArray = FloatArray(size).also { embeddings ->
                    pointer.read(0, embeddings, 0, size)
                }
                return embeddings.toList()
            }

            private fun getTokensPointer(tokens: IntArray): Memory {
                val size: Int = tokens.size * Int.SIZE_BYTES
                val pointer = Memory(size.toLong())
                tokens.forEachIndexed { index, value ->
                    pointer.setInt((index * Int.SIZE_BYTES).toLong(), value)
                }
                return pointer
            }
        }
    }
}
