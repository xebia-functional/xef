package org.xef.xefMobile.services

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.xef.xefMobile.model.*
import org.xef.xefMobile.network.client.HttpClientProvider


class ApiService {

    suspend fun registerUser(request: RegisterRequest): RegisterResponse {
        // Asegúrate de que el tipo RegisterResponse es especificado para que Ktor sepa cómo deserializar la respuesta
        return HttpClientProvider.client.post {
            url("http://localhost:8081/register")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun loginUser(request: LoginRequest): LoginResponse {
        // Asegúrate de que el tipo LoginResponse es especificado para que Ktor sepa cómo deserializar la respuesta
        return HttpClientProvider.client.post {
            url("http://localhost:8081/login")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}