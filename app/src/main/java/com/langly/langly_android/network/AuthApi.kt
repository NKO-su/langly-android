package com.langly.langly_android.network

import com.langly.langly_android.model.AuthResponse
import com.langly.langly_android.model.LoginRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
}