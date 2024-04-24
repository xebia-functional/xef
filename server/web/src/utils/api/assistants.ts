// Este código asume que tienes un enum similar para los endpoints de la API de asistentes
import {
  ApiOptions,
  EndpointsEnum,
  apiConfigConstructor,
  apiFetch,
  baseHeaders,
  defaultApiServer,
} from '@/utils/api';

// Definiciones de tipos para las respuestas y solicitudes de asistentes (ajústalas a tu API)
export type AssistantRequest = {
  name: string;
  // Otros campos relevantes para la creación o actualización de un asistente
};

export type AssistantResponse = {
  id: number;
  name: string;
  createdAt: string;
  // Otros campos que tu API devuelve
};

const assistantApiBaseOptions: ApiOptions = {
  endpointServer: defaultApiServer,
  endpointPath: EndpointsEnum.assistant, // Asegúrate de que este enum existe y es correcto
  endpointValue: '',
  requestOptions: {
    headers: baseHeaders,
  },
};

// POST: Crear un nuevo asistente
export async function postAssistant(authToken: string, data: AssistantRequest): Promise<number> {
  const apiOptions: ApiOptions = {
    ...assistantApiBaseOptions,
    body: JSON.stringify(data),
    requestOptions: {
      method: 'POST',
      ...assistantApiBaseOptions.requestOptions,
      headers: {
        ...assistantApiBaseOptions.requestOptions.headers,
        Authorization: `Bearer ${authToken}`,
      },
    },
  };
  const apiConfig = apiConfigConstructor(apiOptions);
  const response = await apiFetch(apiConfig);
  return response.status; // Asumiendo que la API devuelve un código de estado para representar el resultado
}

// GET: Obtener todos los asistentes
export async function getAssistants(authToken: string): Promise<AssistantResponse[]> {
  const apiOptions: ApiOptions = {
    ...assistantApiBaseOptions,
    requestOptions: {
      method: 'GET',
      ...assistantApiBaseOptions.requestOptions,
      headers: {
        ...assistantApiBaseOptions.requestOptions.headers,
        Authorization: `Bearer ${authToken}`,
      },
    },
  };
  const apiConfig = apiConfigConstructor(apiOptions);
  const response = await apiFetch<AssistantResponse[]>(apiConfig);
  if (!response.data) {
    throw new Error('No assistants data returned from the API');
  }
  return response.data;
}

// PUT: Actualizar un asistente existente
export async function putAssistant(authToken: string, id: number, data: AssistantRequest): Promise<number> {
  const apiOptions: ApiOptions = {
    ...assistantApiBaseOptions,
    endpointValue: `/${id}`,
    body: JSON.stringify(data),
    requestOptions: {
      method: 'PUT',
      ...assistantApiBaseOptions.requestOptions,
      headers: {
        ...assistantApiBaseOptions.requestOptions.headers,
        Authorization: `Bearer ${authToken}`,
      },
    },
  };
  const apiConfig = apiConfigConstructor(apiOptions);
  const response = await apiFetch(apiConfig);
  return response.status;
}

// DELETE: Eliminar un asistente
export async function deleteAssistant(authToken: string, id: number): Promise<number> {
  const apiOptions: ApiOptions = {
    ...assistantApiBaseOptions,
    endpointValue: `/${id}`,
    requestOptions: {
      method: 'DELETE',
      ...assistantApiBaseOptions.requestOptions,
      headers: {
        ...assistantApiBaseOptions.requestOptions.headers,
        Authorization: `Bearer ${authToken}`,
      },
    },
  };
  const apiConfig = apiConfigConstructor(apiOptions);
  const response = await apiFetch(apiConfig);
  return response.status;
}
