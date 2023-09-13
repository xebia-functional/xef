import {
    ApiOptions,
    EndpointsEnum,
    apiConfigConstructor,
    apiFetch,
    baseHeaders,
    defaultApiServer,
  } from '@/utils/api';
    
  const registerApiBaseOptions: ApiOptions = {
    endpointServer: defaultApiServer,
    endpointPath: EndpointsEnum.register,
    endpointValue: '',
    requestOptions: {
      method: 'POST',
      headers: baseHeaders,
    },
  };
  
  export type PostRegisterProps = {
    name: string;
    email: string;
    password: string;
  };
  
  export async function postRegister({
    name,
    email,
    password,
  }: PostRegisterProps): Promise<LoginResponse> {
    const registerRequest: RegisterRequest = {
        name: name,
        email: email,
        password: password,
    };
    const registerApiOptions: ApiOptions = {
      ...registerApiBaseOptions,
      body: JSON.stringify(registerRequest),
      requestOptions: {
        ...registerApiBaseOptions.requestOptions,
        headers: {
          ...registerApiBaseOptions.requestOptions?.headers,
        },
      },
    };
    const registerApiConfig = apiConfigConstructor(
        registerApiOptions,
    );
    const registerResponse = await apiFetch<LoginResponse>(
        registerApiConfig,
    );
  
    return registerResponse;
  }
