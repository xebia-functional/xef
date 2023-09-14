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
    organizations: OrganizationResponse[];
}

type OrganizationResponse = {
    id: number;
    name: string;
}
