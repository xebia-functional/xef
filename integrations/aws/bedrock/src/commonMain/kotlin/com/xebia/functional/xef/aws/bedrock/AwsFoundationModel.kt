package com.xebia.functional.xef.aws.bedrock

enum class AwsFoundationModel(val awsName: String) {
  AmazonTitanTextG1Large("amazon.titan-tg1-large"),
  AmazonTitanTextG1Express("amazon.titan-text-express-v1"),
  AI21LabsJurassic2Mid("ai21.j2-mid-v1"),
  AI21LabsJurassic2Ultra("ai21.j2-ultra-v1"),
  AnthropicClaudeInstantV1("anthropic.claude-instant-v1"),
  AnthropicClaudeV1("anthropic.claude-v1"),
  AnthropicClaudeV2("anthropic.claude-v2"),
  AnthropicClaudeV2_1("anthropic.claude-v2:1"),
  AnthropicClaude3Sonnet20240229V10("anthropic.claude-3-sonnet-20240229-v1:0"),
  CohereCommand("cohere.command-text-v14"),
  StabilityAIStableDiffusionXLV0("stability.stable-diffusion-xl-v0"),
  Empty("empty")
}
