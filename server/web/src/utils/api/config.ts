import { toSnakeCase } from '@/utils/strings';

export const defaultApiServer = 'http://localhost:8081/';

export type ApiKeyProp = {
  apiKey: string;
};

export type ApiConfig = {
  url: URL;
  options?: RequestInit;
};

export type ApiOptions = {
  endpointServer: string;
  endpointPath: Endpoint;
  endpointValue: string;
  body?: string;
  queryParams?: Record<string, string | number | boolean>;
  requestOptions?: RequestInit;
};

export enum EndpointsEnum {
  chatCompletions = 'chat/completions',
}

export type EndpointsTypes = {
  chatCompletions: ChatCompletionsResponse;
};

export type EndpointEnumKey = keyof typeof EndpointsEnum;
export type Endpoint = `${EndpointsEnum}`;

export function apiConfigConstructor(userApiOptions: ApiOptions): ApiConfig {
  const url = new URL(
    `${userApiOptions.endpointServer}${userApiOptions.endpointPath}${userApiOptions.endpointValue}`,
  );

  const queryParams = userApiOptions.queryParams || {};

  Object.entries(queryParams).map(([qpName, qpValue]) => {
    url.searchParams.append(toSnakeCase(qpName), `${qpValue}`);
  });

  const options: RequestInit = {
    ...userApiOptions.requestOptions,
    body: userApiOptions.body || userApiOptions.requestOptions?.body,
  };

  const config = {
    url,
    options,
  };

  return config;
}

export async function fetchWithTimeout(
  input: RequestInfo | URL,
  init?: RequestInit & { timeout?: number },
): Promise<Response> {
  const { timeout = 8000 } = init || { timeout: 8000 };

  const controller = new AbortController();
  const id = setTimeout(() => controller.abort(), timeout);

  const response = await fetch(input, {
    ...init,
    signal: controller.signal,
  });
  clearTimeout(id);

  return response;
}

export type ErrorResponse = {
  error: {
    message: string;
    type: string;
    param: unknown;
    code: string;
  };
};

const isErrorResponse = (b: unknown): b is ErrorResponse => {
  return (b as ErrorResponse).error !== undefined;
};

export async function apiFetch<T = Record<string, unknown>>(
  userApiConfig: ApiConfig,
): Promise<T> {
  const apiConfig = {
    ...userApiConfig,
    options: {
      ...(userApiConfig && userApiConfig.options),
      timeout: 60000,
    },
  };

  try {
    const response = await fetchWithTimeout(apiConfig.url, apiConfig.options);
    const responseData: T | ErrorResponse = await response.json();

    if (isErrorResponse(responseData)) throw responseData.error.message;

    return responseData;
  } catch (error) {
    const errorMessage = `💢 Error: ${error}`;
    console.error(errorMessage);
    throw errorMessage;
  }
}
