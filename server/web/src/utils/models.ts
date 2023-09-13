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
