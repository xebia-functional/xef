package com.xebia.functional.tokenizer.internal

private const val ENDOFTEXT = "<|endoftext|>"
private const val FIM_PREFIX = "<|fim_prefix|>"
private const val FIM_MIDDLE = "<|fim_middle|>"
private const val FIM_SUFFIX = "<|fim_suffix|>"
private const val ENDOFPROMPT = "<|endofprompt|>"

internal val SPECIAL_TOKENS_X50K_BASE: Map<String, Int> = HashMap<String, Int>(1).apply {
    put(ENDOFTEXT, 50256)
}

internal val SPECIAL_TOKENS_P50K_EDIT: Map<String, Int> = HashMap<String, Int>(4).apply {
    put(ENDOFTEXT, 50256)
    put(FIM_PREFIX, 50281)
    put(FIM_MIDDLE, 50282)
    put(FIM_SUFFIX, 50283)
}

internal val SPECIAL_TOKENS_CL100K_BASE: Map<String, Int> = HashMap<String, Int>(5).apply {
    put(ENDOFTEXT, 100257)
    put(FIM_PREFIX, 100258)
    put(FIM_MIDDLE, 100259)
    put(FIM_SUFFIX, 100260)
    put(ENDOFPROMPT, 100276)
}

internal val SPECIAL_TOKENS_O200K_BASE: Map<String, Int> = HashMap<String, Int>(2).apply {
    put(ENDOFTEXT, 199999);
    put(ENDOFPROMPT, 200018);
}
