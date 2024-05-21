import {
    ApiOptions,
    EndpointsEnum,
    apiConfigConstructor,
    apiFetch,
    baseHeaders,
    defaultApiServer,
  } from '@/utils/api';

  export type CreateAssistantRequest = {
    model: string;
    name?: string;
    description?: string;
    instructions?: string;
    tools?: AssistantTool[];
    fileIds?: string[];
    metadata?: Record<string, string>;
  };

  export type ModifyAssistantRequest = {
    model?: string;
    name?: string;
    description?: string;
    instructions?: string;
    tools?: AssistantTool[];
    fileIds?: string[];
    metadata?: Record<string, string>;
  };

  type CodeInterpreter = {
    type: 'code_interpreter';
  };

  type Retrieval = {
    type: 'retrieval';
  };

  type Function = {
    type: 'function';
    name: string;
    description: string;
    parameters: string;
  };

  type AssistantTool = CodeInterpreter | Retrieval | Function;

  const assistantApiBaseOptions: ApiOptions = {
    endpointServer: defaultApiServer,
    endpointPath: EndpointsEnum.assistant,
    endpointValue: '',
    requestOptions: {
      headers: baseHeaders,
    },
  };

  async function executeRequest<T>(authToken: string, method: string, endpointValue: string = '', body?: any): Promise<T> {
    const apiOptions: ApiOptions = {
      ...assistantApiBaseOptions,
      endpointValue: endpointValue,
      body: body ? JSON.stringify(body) : undefined,
      requestOptions: {
        method: method,
        ...assistantApiBaseOptions.requestOptions,
        headers: {
          ...assistantApiBaseOptions.requestOptions.headers,
          Authorization: `Bearer ${authToken}`,
        },
      },
    };
    const apiConfig = apiConfigConstructor(apiOptions);
    const response = await apiFetch<T>(apiConfig);
    if (!response.data) {
      throw new Error('No data returned from the API');
    }
    return response.data;
  }

  export async function postAssistant(authToken: string, data: CreateAssistantRequest): Promise<AssistantObject> {
    return executeRequest<AssistantResponse>(authToken, 'POST', '', data);
  }

  export async function getAssistants(authToken: string): Promise<ListAssistantResponse> {
    return executeRequest<AssistantResponse[]>(authToken, 'GET');
  }

  export async function putAssistant(authToken: string, id: string, data: ModifyAssistantRequest): Promise<AssistantObject> {
    return executeRequest<AssistantResponse>(authToken, 'PUT', `/${id}`, data);
  }

  export async function deleteAssistant(authToken: string, id: string): Promise<DeleteAssistantResponse> {
    await executeRequest<void>(authToken, 'DELETE', `/${id}`);
  }