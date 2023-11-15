type RegisterRequest = {
    name: string;
    email: string;
    password: string;
}

type LoginRequest = {
    email: string;
    password: string;
}

type LoginResponse = {
    authToken: string;
}

type OrganizationRequest = {
    name: string;
}

type OrganizationResponse = {
    id: number;
    name: string;
    users: number;
}

type ProjectRequest = {
    name: string;
    orgId: number;
}

type PutProjectRequest = {
    name: string;
    orgId?: number;
}

type ProjectResponse = {
    id: number;
    name: string;
    org: OrganizationResponse;
}
