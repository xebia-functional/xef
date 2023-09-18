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
