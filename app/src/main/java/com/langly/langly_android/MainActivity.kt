package com.langly.langly_android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.langly.langly_android.model.RefreshRequest
import com.langly.langly_android.network.RetrofitInstance
import com.langly.langly_android.network.TokenManager
import com.langly.langly_android.ui.main.LanglyApp
import com.langly.langly_android.ui.theme.LanglyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (savedInstanceState == null) handleAuthRedirect(intent)

        setContent {
            LanglyTheme {
                LanglyApp(onLogout = { performLogout() })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAuthRedirect(intent)
    }

    private fun handleAuthRedirect(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != "ourilang" || uri.host != "auth") return

        val access = uri.getQueryParameter("access")
        val refresh = uri.getQueryParameter("refresh")
        if (access.isNullOrBlank() || refresh.isNullOrBlank()) return

        lifecycleScope.launch { TokenManager.saveTokens(access, refresh) }
    }

    private suspend fun performLogout() {
        try {
            val refresh = TokenManager.getRefreshToken()
            if (refresh != null) {
                RetrofitInstance.authApi.logout(RefreshRequest(refresh))
            }
        } catch (e: Exception) {
        }
        TokenManager.clearTokens()
    }
}