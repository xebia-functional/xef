package com.xebia.functional.gpt4all.llama

import com.sun.jna.Memory
import com.xebia.functional.gpt4all.LlamaConfig
import com.xebia.functional.gpt4all.LlamaGenerationConfig
import com.xebia.functional.gpt4all.getModelName
import com.xebia.functional.gpt4all.llama.libraries.LlamaLibrary
import com.xebia.functional.gpt4all.loadLlamaLibrary
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
            override val context: LlamaContext = LlamaContext(library, LlamaConfig(path.toString(), embedding = true))
            override val name: String = path.getModelName()

            override fun embeddings(text: String, generationConfig: LlamaGenerationConfig): List<Float> {
                val tokens: IntArray = encode(text, false).toIntArray()
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

            private fun LlamaLibrary.getEmbeddings(): List<Float> {
                val size: Int = llama_n_embd(context.pointer)
                val pointer = llama_get_embeddings(context.pointer)
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

//fun tokenize(context: LlamaContext, text: String, addBos: Boolean): List<Int> {
//    val tokensSize: Int = text.length + if (addBos) 1 else 0
//    val tokensPointer: Pointer = pointerFromTokenSize(tokensSize)
//    val textPointer: Pointer = pointerFromText(text)
//
//    val nTokens: Int = llamaLibrary.llama_tokenize(
//        context.pointer,
//        textPointer,
//        tokensPointer,
//        tokensSize,
//        addBos
//    )
//    val tokens: IntArray = tokensPointer.getIntArray(0, nTokens)
//    tokensPointer.clear(tokensSize.toLong())
//    return tokens.toList()
//}

//private fun IntArray.toPointer(): Pointer {
//    val size: Int = size * Native.getNativeSize(Int::class.java)
//    val pointer = Pointer(Native.malloc(size.toLong()))
//    pointer.write(0, this, 0, size)
//    return pointer
//}
//
//private fun pointerFromText(text: String): Pointer {
//    val textBytes: ByteArray = text.toByteArray()
//    val textBuffer: ByteBuffer = ByteBuffer.allocateDirect(textBytes.size)
//    textBuffer.put(textBytes)
//    textBuffer.flip()
//    return Native.getDirectBufferPointer(textBuffer)
//}
//
//private fun pointerFromTokenSize(size: Int): Pointer {
//    return Memory(size.toLong() * Native.getNativeSize(Long::class.java))
//}
