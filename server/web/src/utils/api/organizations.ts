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

export async function postOrganizations({
  name,
}: PostOrganizationProps): Promise<number> {
  const createOrganizationRequest: CreateOrganizationRequest = {
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

export async function getOrganizations(): Promise<OrganizationsResponse> {
  const getOrganizationApiOptions: ApiOptions = {
    ...orgApiBaseOptions,
    requestOptions: {
      method: 'GET',
      ...orgApiBaseOptions.requestOptions,
      headers: {
        ...orgApiBaseOptions.requestOptions?.headers,
      },
    },
  };
  const getOrganizationApiConfig = apiConfigConstructor(
    getOrganizationApiOptions,
  );

  const organizationsResponse = await apiFetch<OrganizationsResponse>(
    getOrganizationApiConfig,
  );

  if (organizationsResponse.data == null) {
    throw new Error('Organizations data is null');
  }

  return organizationsResponse.data!;
}
