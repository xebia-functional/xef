import {
  ApiOptions,
  EndpointsEnum,
  apiConfigConstructor,
  apiFetch,
  baseHeaders,
  defaultApiServer,
} from '@/utils/api';

// Create Organization Endpoint

const orgApiBaseOptions: ApiOptions = {
  endpointServer: defaultApiServer,
  endpointPath: EndpointsEnum.organization,
  endpointValue: '',
  requestOptions: {
    headers: baseHeaders,
  },
};

export type PostOrganizationProps = {
  name: string;
};

export async function postOrganizations(authToken: string, {
  name,
}: PostOrganizationProps): Promise<number> {
  const createOrganizationRequest: OrganizationRequest = {
    name: name,
  };
  const createOrganizationApiOptions: ApiOptions = {
    ...orgApiBaseOptions,
    body: JSON.stringify(createOrganizationRequest),
    requestOptions: {
      method: 'POST',
      ...orgApiBaseOptions.requestOptions,
      headers: {
        ...orgApiBaseOptions.requestOptions?.headers,
        Authorization: `Bearer ${authToken}`,
      },
    },
  };
  const createOrganizationApiConfig = apiConfigConstructor(
    createOrganizationApiOptions,
  );
  const createOrganizationResponse = await apiFetch<LoginResponse>(
    createOrganizationApiConfig,
  );

  return createOrganizationResponse.status;
}

// Get Organizations Endpoint

export async function getOrganizations(authToken: string): Promise<OrganizationResponse[]> {
  console.info('getOrganizations');
  const getOrganizationApiOptions: ApiOptions = {
    ...orgApiBaseOptions,
    requestOptions: {
      method: 'GET',
      ...orgApiBaseOptions.requestOptions,
      headers: {
        ...orgApiBaseOptions.requestOptions?.headers,
        Authorization: `Bearer ${authToken}`,
      },
    },
  };
  const getOrganizationApiConfig = apiConfigConstructor(
    getOrganizationApiOptions,
  );

  const organizationsResponse = await apiFetch<OrganizationResponse[]>(
    getOrganizationApiConfig,
  );

  if (organizationsResponse.data == null) {
    throw new Error('Organizations data is null');
  }

  return organizationsResponse.data!;
}

// put organization endpoint

export async function putOrganizations(authToken: string, id: number, {
  name,
}: PostOrganizationProps): Promise<number> {
  const putOrganizationRequest: OrganizationRequest = {
    name: name,
  };
  const putOrganizationApiOptions: ApiOptions = {
    ...orgApiBaseOptions,
    endpointValue: `/${id}`,
    body: JSON.stringify(putOrganizationRequest),
    requestOptions: {
      method: 'PUT',
      ...orgApiBaseOptions.requestOptions,
      headers: {
        ...orgApiBaseOptions.requestOptions?.headers,
        Authorization: `Bearer ${authToken}`,
      },
    },
  };
  const putOrganizationApiConfig = apiConfigConstructor(
    putOrganizationApiOptions,
  );
  const putOrganizationResponse = await apiFetch<LoginResponse>(
    putOrganizationApiConfig,
  );

  return putOrganizationResponse.status;
}
