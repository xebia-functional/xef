import {
  ApiOptions,
  EndpointsEnum,
  apiConfigConstructor,
  apiFetch,
  baseHeaders,
  defaultApiServer,
} from '@/utils/api';

// Create Project Endpoint

const projectApiBaseOptions: ApiOptions = {
  endpointServer: defaultApiServer,
  endpointPath: EndpointsEnum.Projects,
  endpointValue: '',
  requestOptions: {
    headers: baseHeaders,
  },
};

export type PostProjectProps = {
  name: string;
  orgId: number;
};

export async function postProjects(authToken: string, {
  name,
  orgId,
}: PostProjectProps): Promise<number> {
  const createProjectRequest: ProjectRequest = {
    name: name,
    orgId: orgId,
  };
  const createProjectApiOptions: ApiOptions = {
    ...projectApiBaseOptions,
    body: JSON.stringify(createProjectRequest),
    requestOptions: {
      method: 'POST',
      ...projectApiBaseOptions.requestOptions,
      headers: {
        ...projectApiBaseOptions.requestOptions?.headers,
        Authorization: `Bearer ${authToken}`,
      },
    },
  };
  const createProjectApiConfig = apiConfigConstructor(
    createProjectApiOptions,
  );
  const createProjectResponse = await apiFetch(
    createProjectApiConfig,
  );

  return createProjectResponse.status;
}

// Get Projects Endpoint

export async function getProjects(authToken: string): Promise<ProjectResponse[]> {
  const getProjectApiOptions: ApiOptions = {
    ...projectApiBaseOptions,
    requestOptions: {
      method: 'GET',
      ...projectApiBaseOptions.requestOptions,
      headers: {
        ...projectApiBaseOptions.requestOptions?.headers,
        Authorization: `Bearer ${authToken}`,
      },
    },
  };
  const getProjectApiConfig = apiConfigConstructor(
    getProjectApiOptions,
  );

  const projectsResponse = await apiFetch<ProjectResponse[]>(
    getProjectApiConfig,
  );

  if (projectsResponse.data == null) {
    throw new Error('Project data is null');
  }

  return projectsResponse.data!;
}

// put project endpoint

export type PutProjectProps = {
  name: string;
  orgId?: number;
};

export async function putProjects(authToken: string, id: number, {
  name,
  orgId,
}: PutProjectProps): Promise<number> {
  const putProjectRequest: PutProjectRequest = {
    name: name,
    orgId: orgId,
  };
  const putProjectApiOptions: ApiOptions = {
    ...projectApiBaseOptions,
    endpointValue: `/${id}`,
    body: JSON.stringify(putProjectRequest),
    requestOptions: {
      method: 'PUT',
      ...projectApiBaseOptions.requestOptions,
      headers: {
        ...projectApiBaseOptions.requestOptions?.headers,
        Authorization: `Bearer ${authToken}`,
      },
    },
  };
  const putProjectApiConfig = apiConfigConstructor(
    putProjectApiOptions,
  );
  const putProjectResponse = await apiFetch(
    putProjectApiConfig,
  );

  return putProjectResponse.status;
}


// delete project endpoint

export async function deleteProjects(authToken: string, id: number): Promise<number> {
  const deleteProjectApiOptions: ApiOptions = {
    ...projectApiBaseOptions,
    endpointValue: `/${id}`,
    requestOptions: {
      method: 'DELETE',
      ...projectApiBaseOptions.requestOptions,
      headers: {
        ...projectApiBaseOptions.requestOptions?.headers,
        Authorization: `Bearer ${authToken}`,
      },
    },
  };
  const deleteProjectApiConfig = apiConfigConstructor(
    deleteProjectApiOptions,
  );
  const deleteProjectResponse = await apiFetch(
    deleteProjectApiConfig,
  );

  return deleteProjectResponse.status;
}
