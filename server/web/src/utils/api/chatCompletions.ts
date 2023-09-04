import {
  ApiKeyProp,
  ApiOptions,
  EndpointsEnum,
  apiConfigConstructor,
  apiFetch,
  defaultApiServer,
} from '@/utils/api';

const baseHeaders = {
  Accept: 'application/json',
  'Content-Type': 'application/json',
};

const chatCompletionsBaseRequest: ChatCompletionsRequest = {
  model: 'gpt-3.5-turbo-16k',
  messages: [
    {
      role: 'user',
      content: '',
      name: 'USER',
    },
  ],
  temperature: 0.4,
  top_p: 1.0,
  n: 1,
  max_tokens: 12847,
  presence_penalty: 0.0,
  frequency_penalty: 0.0,
  logit_bias: {},
  user: 'USER',
};

const chatCompletionsApiBaseOptions: ApiOptions = {
  endpointServer: defaultApiServer,
  endpointPath: EndpointsEnum.chatCompletions,
  endpointValue: '',
  requestOptions: {
    method: 'POST',
    headers: baseHeaders,
  },
};

export type PostChatCompletionsProps = ApiKeyProp & {
  prompt: string;
};

export async function postChatCompletions({
  prompt,
  apiKey,
}: PostChatCompletionsProps): Promise<ChatCompletionsResponse> {
  const chatCompletionsRequest: ChatCompletionsRequest = {
    ...chatCompletionsBaseRequest,
    messages: [
      {
        ...chatCompletionsBaseRequest.messages[0],
        content: prompt,
      },
    ],
  };
  const chatCompletionsApiOptions: ApiOptions = {
    ...chatCompletionsApiBaseOptions,
    body: JSON.stringify(chatCompletionsRequest),
    requestOptions: {
      ...chatCompletionsApiBaseOptions.requestOptions,
      headers: {
        ...chatCompletionsApiBaseOptions.requestOptions?.headers,
        Authorization: `Bearer ${apiKey}`,
      },
    },
  };
  const chatCompletionsApiConfig = apiConfigConstructor(
    chatCompletionsApiOptions,
  );
  const chatCompletionResponse = await apiFetch<ChatCompletionsResponse>(
    chatCompletionsApiConfig,
  );

  return chatCompletionResponse;
}
