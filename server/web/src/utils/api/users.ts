import {
  ApiOptions,
  EndpointsEnum,
  apiConfigConstructor,
  apiFetch,
  baseHeaders,
  defaultApiServer,
} from '@/utils/api';

// Login Endpoint

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
  const loginResponse = await apiFetch<LoginResponse>(
    loginApiConfig,
  );

  if (loginResponse.data == null) {
    throw new Error('Login response data is null');
  }

  return loginResponse.data!;
}

// Register Endpoint

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

  if (registerResponse.data == null) {
    throw new Error('Register response data is null');
  }

  return registerResponse.data!;
}
