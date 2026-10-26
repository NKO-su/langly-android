package com.langly.langly_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.langly.langly_android.network.AuthState
import com.langly.langly_android.network.TokenManager
import com.langly.langly_android.ui.article.ArticleDetailScreen
import com.langly.langly_android.ui.article.ArticleListScreen
import com.langly.langly_android.ui.article.ArticleViewModel
import com.langly.langly_android.ui.login.LoginScreen
import com.langly.langly_android.ui.register.RegisterScreen
import com.langly.langly_android.ui.theme.LanglyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LanglyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val authState by TokenManager.authState
                        .collectAsState(initial = AuthState.Loading)

                    var selectedArticleId by remember { mutableStateOf<Long?>(null) }
                    var showRegister by remember { mutableStateOf(false) }

                    // Bị đá ra (hoặc logout) -> xóa trạng thái màn hình cũ
                    LaunchedEffect(authState) {
                        if (authState == AuthState.LoggedOut) {
                            selectedArticleId = null
                            showRegister = false
                        }
                    }

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
}