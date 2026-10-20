package com.langly.langly_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.langly.langly_android.ui.article.ArticleDetailScreen
import com.langly.langly_android.ui.article.ArticleListScreen
import com.langly.langly_android.ui.article.ArticleViewModel
import com.langly.langly_android.ui.login.LoginScreen
import com.langly.langly_android.ui.theme.LanglyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LanglyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var isLoggedIn by remember { mutableStateOf(false) }
                    var selectedArticleId by remember { mutableStateOf<Long?>(null) }

                    if (isLoggedIn) {
                        // Chỉ tạo SAU khi đăng nhập: init của ViewModel gọi API ngay
                        val articleViewModel: ArticleViewModel = viewModel()
                        // Copy ra biến cục bộ vì delegated property không smart cast được
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
                    } else {
                        LoginScreen(
                            modifier = Modifier.padding(innerPadding),
                            onLoginSuccess = { isLoggedIn = true }
                        )
                    }
                }
            }
        }
    }
}