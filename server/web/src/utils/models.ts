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

type CreateOrganizationRequest = {
    name: string;
}

type OrganizationsResponse = {
    items: OrganizationResponse[];
}

type OrganizationResponse = {
    id: number;
    name: string;
}
