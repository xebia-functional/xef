import {
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
  
  const loginApiBaseOptions: ApiOptions = {
    endpointServer: defaultApiServer,
    endpointPath: EndpointsEnum.login,
    endpointValue: '',
    requestOptions: {
      method: 'POST',
      headers: baseHeaders,
    },
  };
  
  export type PostLoginProps = {
    email: string;
    password: string;
  };
  
  export async function postLogin({
    email,
    password,
  }: PostLoginProps): Promise<LoginResponse> {
    const loginRequest: LoginRequest = {
        email: email,
        password: password,
    };
    const loginApiOptions: ApiOptions = {
      ...loginApiBaseOptions,
      body: JSON.stringify(loginRequest),
      requestOptions: {
        ...loginApiBaseOptions.requestOptions,
        headers: {
          ...loginApiBaseOptions.requestOptions?.headers,
        },
      },
    };
    const loginApiConfig = apiConfigConstructor(
        loginApiOptions,
    );
    const LoginResponse = await apiFetch<LoginResponse>(
        loginApiConfig,
    );
  
    return LoginResponse;
  }
