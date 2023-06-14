package com.xebia.functional.gpt4all.llama

import com.sun.jna.Memory
import com.xebia.functional.gpt4all.LlamaConfig
import com.xebia.functional.gpt4all.getModelName
import com.xebia.functional.gpt4all.llama.libraries.LlamaLibrary
import com.xebia.functional.gpt4all.loadLlamaLibrary
import java.nio.file.Path

interface LlamaModel : AutoCloseable {
    val library: LlamaLibrary
    val context: LlamaContext
    val name: String

    fun embeddings(text: String): List<Float>
    fun tokenize(text: String, addBos: Boolean): List<Int>

    companion object {
        operator fun invoke(
            path: Path
        ): LlamaModel = object : LlamaModel {
            override val library: LlamaLibrary = loadLlamaLibrary()
            override val context: LlamaContext = LlamaContext(library, LlamaConfig(path.toString()))
            override val name: String = path.getModelName()

            override fun embeddings(text: String): List<Float> {
                TODO("Not yet implemented")
            }

            override fun tokenize(text: String, addBos: Boolean): List<Int> {
                val nMaxTokens: Int = text.length + if (addBos) 1 else 0
                val cText: ByteArray = text.toByteArray()
                val textPointer: Memory = Memory(cText.size.toLong())
                    .apply { write(0, cText, 0, cText.size) }
                val tokensPointer = Memory(nMaxTokens * Int.SIZE_BYTES.toLong())
                val n: Int = library.llama_tokenize(context.pointer, textPointer, tokensPointer, nMaxTokens, addBos)
                check(n >= 0)
                return (0 until n).map { tokensPointer.getInt(it * Int.SIZE_BYTES.toLong()) }
            }

            override fun close(): Unit =
                library.llama_free(context.pointer)
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
