package com.langly.langly_android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.langly.langly_android.model.RefreshRequest
import com.langly.langly_android.network.AuthState
import com.langly.langly_android.network.RetrofitInstance
import com.langly.langly_android.network.TokenManager
import com.langly.langly_android.ui.article.ArticleDetailScreen
import com.langly.langly_android.ui.article.ArticleListScreen
import com.langly.langly_android.ui.article.ArticleViewModel
import com.langly.langly_android.ui.login.LoginScreen
import com.langly.langly_android.ui.register.RegisterScreen
import com.langly.langly_android.ui.theme.LanglyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (savedInstanceState == null) handleAuthRedirect(intent)

        setContent {
            LanglyTheme {
                val authFlow = remember { TokenManager.authState }
                val authState by authFlow.collectAsState(initial = AuthState.Loading)

                var selectedArticleId by remember { mutableStateOf<Long?>(null) }
                var showRegister by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()
                LaunchedEffect(authState) {
                    if (authState == AuthState.LoggedOut) {
                        selectedArticleId = null
                        showRegister = false
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (authState == AuthState.LoggedIn && selectedArticleId == null) {
                            TopAppBar(
                                title = { Text("Ourilang") },
                                actions = {
                                    TextButton(onClick = { scope.launch { performLogout() } }) {
                                        Text("Đăng xuất")
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    when (authState) {
                        AuthState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        AuthState.LoggedIn -> {
                            val articleViewModel: ArticleViewModel = viewModel()
                            val articleId = selectedArticleId

                            BackHandler(enabled = articleId != null) {
                                selectedArticleId = null
                            }

                            if (articleId == null) {
                                ArticleListScreen(
                                    onArticleClick = { selectedArticleId = it },
                                    modifier = Modifier.padding(innerPadding),
                                    viewModel = articleViewModel
                                )
                            } else {
                                ArticleDetailScreen(
                                    articleId = articleId,
                                    onBackToList = {
                                        articleViewModel.refresh()
                                        selectedArticleId = null
                                    },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }

                        AuthState.LoggedOut -> {
                            BackHandler(enabled = showRegister) {
                                showRegister = false
                            }

                            if (showRegister) {
                                RegisterScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    onBackToLogin = { showRegister = false }
                                )
                            } else {
                                LoginScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    // Đăng nhập xong, token được lưu -> authState tự chuyển LoggedIn
                                    onLoginSuccess = { },
                                    onGoToRegister = { showRegister = true }
                                )
                            }
                        }
                    }
                }
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