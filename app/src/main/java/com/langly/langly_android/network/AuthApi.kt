package com.langly.langly_android.network

import com.langly.langly_android.model.AuthResponse
import com.langly.langly_android.model.LoginRequest
import com.langly.langly_android.model.RegisterRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ResponseBody>

    @POST("api/auth/resend-verification")
    suspend fun resendVerification(@Query("email") email: String): Response<ResponseBody>
}