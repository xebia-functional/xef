package com.xebia.functional.gpt4all.libraries

import com.sun.jna.Library
import com.sun.jna.Pointer

interface LlamaLibrary : Library {
    fun llama_n_embd(context: Pointer): Int
    fun llama_get_embeddings(context: Pointer): Pointer
}
