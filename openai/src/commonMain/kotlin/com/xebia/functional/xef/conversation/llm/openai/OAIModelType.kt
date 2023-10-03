// package com.xebia.functional.xef.conversation.llm.openai
//
// import com.xebia.functional.tokenizer.Encoding
// import com.xebia.functional.tokenizer.EncodingType
// import com.xebia.functional.tokenizer.ModelType
//
// sealed class OAIModelType(
//  name: String,
//  val encodingType: EncodingType,
//  maxInputTokens: Int,
//  maxOutputTokens: Int,
//  tokensPerMessage: Int = 0,
//  tokensPerName: Int = 0,
//  tokenPadding: Int = 20,
// ) :
//  ModelType(name, maxInputTokens, maxOutputTokens, tokensPerMessage, tokensPerName, tokenPadding)
// {
//
//  inline val encoding: Encoding
//    inline get() = encodingType.encoding
//
//  // chat
//  object GPT_4 :
//    OAIModelType(
//      "gpt-4",
//      EncodingType.CL100K_BASE,
//      8192,
//      tokensPerMessage = 3,
//      tokensPerName = 2,
//      tokenPadding = 5
//    )
//
//  object GPT_4_0314 :
//    OAIModelType(
//      "gpt-4-0314",
//      EncodingType.CL100K_BASE,
//      8192,
//      tokensPerMessage = 3,
//      tokensPerName = 2,
//      tokenPadding = 5
//    )
//
//  object GPT_4_32K :
//    OAIModelType(
//      "gpt-4-32k",
//      EncodingType.CL100K_BASE,
//      32768,
//      tokensPerMessage = 3,
//      tokensPerName = 2,
//      tokenPadding = 5
//    )
//
//  object GPT_3_5_TURBO :
//    OAIModelType(
//      "gpt-3.5-turbo",
//      EncodingType.CL100K_BASE,
//      4097,
//      tokensPerMessage = 4,
//      tokensPerName = 0,
//      tokenPadding = 5
//    )
//
//  object GPT_3_5_TURBO_16_K : OAIModelType("gpt-3.5-turbo-16k", EncodingType.CL100K_BASE, 4097 *
// 4)
//
//  object GPT_3_5_TURBO_FUNCTIONS :
//    OAIModelType(
//      "gpt-3.5-turbo-0613",
//      EncodingType.CL100K_BASE,
//      4097,
//      tokensPerMessage = 4,
//      tokensPerName = 0,
//      tokenPadding = 200
//    )
//
//  // text
//  object TEXT_DAVINCI_003 : OAIModelType("text-davinci-003", EncodingType.P50K_BASE, 4097)
//
//  object TEXT_DAVINCI_002 : OAIModelType("text-davinci-002", EncodingType.P50K_BASE, 4097)
//
//  object TEXT_DAVINCI_001 : OAIModelType("text-davinci-001", EncodingType.R50K_BASE, 2049)
//
//  object TEXT_CURIE_001 : OAIModelType("text-curie-001", EncodingType.R50K_BASE, 2049)
//
//  object TEXT_BABBAGE_001 : OAIModelType("text-babbage-001", EncodingType.R50K_BASE, 2049)
//
//  object TEXT_ADA_001 : OAIModelType("text-ada-001", EncodingType.R50K_BASE, 2049)
//
//  object DAVINCI : OAIModelType("davinci", EncodingType.R50K_BASE, 2049)
//
//  object CURIE : OAIModelType("curie", EncodingType.R50K_BASE, 2049)
//
//  object BABBAGE : OAIModelType("babbage", EncodingType.R50K_BASE, 2049)
//
//  object ADA : OAIModelType("ada", EncodingType.R50K_BASE, 2049)
//
//  // code
//  object CODE_DAVINCI_002 : OAIModelType("code-davinci-002", EncodingType.P50K_BASE, 8001)
//
//  object CODE_DAVINCI_001 : OAIModelType("code-davinci-001", EncodingType.P50K_BASE, 8001)
//
//  object CODE_CUSHMAN_002 : OAIModelType("code-cushman-002", EncodingType.P50K_BASE, 2048)
//
//  object CODE_CUSHMAN_001 : OAIModelType("code-cushman-001", EncodingType.P50K_BASE, 2048)
//
//  object DAVINCI_CODEX : OAIModelType("davinci-codex", EncodingType.P50K_BASE, 4096)
//
//  object CUSHMAN_CODEX : OAIModelType("cushman-codex", EncodingType.P50K_BASE, 2048)
//
//  // edit
//  object TEXT_DAVINCI_EDIT_001 :
//    OAIModelType("text-davinci-edit-001", EncodingType.P50K_EDIT, 3000)
//
//  object CODE_DAVINCI_EDIT_001 :
//    OAIModelType("code-davinci-edit-001", EncodingType.P50K_EDIT, 3000)
//
//  // embeddings
//  object TEXT_EMBEDDING_ADA_002 :
//    OAIModelType("text-embedding-ada-002", EncodingType.CL100K_BASE, 8191)
//
//  // old embeddings
//  object TEXT_SIMILARITY_DAVINCI_001 :
//    OAIModelType("text-similarity-davinci-001", EncodingType.R50K_BASE, 2046)
//
//  object TEXT_SIMILARITY_CURIE_001 :
//    OAIModelType("text-similarity-curie-001", EncodingType.R50K_BASE, 2046)
//
//  object TEXT_SIMILARITY_BABBAGE_001 :
//    OAIModelType("text-similarity-babbage-001", EncodingType.R50K_BASE, 2046)
//
//  object TEXT_SIMILARITY_ADA_001 :
//    OAIModelType("text-similarity-ada-001", EncodingType.R50K_BASE, 2046)
//
//  object TEXT_SEARCH_DAVINCI_DOC_001 :
//    OAIModelType("text-search-davinci-doc-001", EncodingType.R50K_BASE, 2046)
//
//  object TEXT_SEARCH_CURIE_DOC_001 :
//    OAIModelType("text-search-curie-doc-001", EncodingType.R50K_BASE, 2046)
//
//  object TEXT_SEARCH_BABBAGE_DOC_001 :
//    OAIModelType("text-search-babbage-doc-001", EncodingType.R50K_BASE, 2046)
//
//  object TEXT_SEARCH_ADA_DOC_001 :
//    OAIModelType("text-search-ada-doc-001", EncodingType.R50K_BASE, 2046)
//
//  object CODE_SEARCH_BABBAGE_CODE_001 :
//    OAIModelType("code-search-babbage-code-001", EncodingType.R50K_BASE, 2046)
//
//  object CODE_SEARCH_ADA_CODE_001 :
//    OAIModelType("code-search-ada-code-001", EncodingType.R50K_BASE, 2046)
// }
