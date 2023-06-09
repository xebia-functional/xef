package com.xebia.functional.gpt4all.llama

import com.sun.jna.Library
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.xebia.functional.gpt4all.llama.libraries.LlamaLibrary
import com.xebia.functional.gpt4all.llmodel.GPT4AllModel
import java.nio.ByteBuffer
import java.nio.file.Path

//sealed interface LlamaModel : AutoCloseable {
//    val library: LlamaLibrary
//    val context: LlamaContext
//
//    fun embeddings(text: String): List<Int>
//    fun tokenize(text: String):
//}
//
//interface MPTModelInternal : GPT4AllModel {
//
//}
//
//
//
//private fun llamaTokenize(
//    context: Pointer, text: String, addBos: Boolean
//): List<Int> {
//    val nMaxTokens = text.length + if (addBos) 1 else 0
//    val cText = text.toByteArray()
//    val textPointer = Memory(cText.size.toLong()).apply { write(0, cText, 0, cText.size) }
//    val tokensPointer = Memory(nMaxTokens * Int.SIZE_BYTES.toLong())
//    val n = llamaLibrary.llama_tokenize(context, textPointer, tokensPointer, nMaxTokens, addBos)
//    check(n >= 0)
//    return (0 until n).map { tokensPointer.getInt(it * Int.SIZE_BYTES.toLong()) }
//
//}
//
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



private fun GPT4AllModel.loadLlamaContext(path: Path): LlamaContext =
    LlamaContext(llamaLibrary, ModelLoad(path.toString()))

private fun IntArray.toPointer(): Pointer {
    val size: Int = size * Native.getNativeSize(Int::class.java)
    val pointer = Pointer(Native.malloc(size.toLong()))
    pointer.write(0, this, 0, size)
    return pointer
}

private fun pointerFromText(text: String): Pointer {
    val textBytes: ByteArray = text.toByteArray()
    val textBuffer: ByteBuffer = ByteBuffer.allocateDirect(textBytes.size)
    textBuffer.put(textBytes)
    textBuffer.flip()
    return Native.getDirectBufferPointer(textBuffer)
}

private fun pointerFromTokenSize(size: Int): Pointer {
    return Memory(size.toLong() * Native.getNativeSize(Long::class.java))
}
