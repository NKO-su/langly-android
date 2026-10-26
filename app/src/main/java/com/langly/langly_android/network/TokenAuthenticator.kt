package com.langly.langly_android.network

import com.langly.langly_android.model.RefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.io.IOException

class TokenAuthenticator(
    private val refreshApi: AuthApi
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.priorResponse != null) return null

        val refreshToken = runBlocking { TokenManager.getRefreshToken() } ?: return null

        val refreshResponse = try {
            runBlocking { refreshApi.refresh(RefreshRequest(refreshToken)) }
        } catch (e: IOException) {
            return null
        }

        if (!refreshResponse.isSuccessful) {
            if (refreshResponse.code() in 400..499) {
                runBlocking { TokenManager.clearTokens() }
            }
            return null
        }

        val body = refreshResponse.body() ?: return null

        runBlocking { TokenManager.saveTokens(body.accessToken, body.refreshToken) }

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${body.accessToken}")
            .build()
    }
}