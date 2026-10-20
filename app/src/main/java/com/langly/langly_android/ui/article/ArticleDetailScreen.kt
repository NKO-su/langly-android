package com.langly.langly_android.ui.article

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ArticleDetailScreen(
    articleId: Long,
    onBackToList: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArticleDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(articleId) {
        viewModel.load(articleId)
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val s = state) {
            ArticleDetailUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is ArticleDetailUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(s.article.title, style = MaterialTheme.typography.headlineSmall)
                    s.article.publishedAt?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                    s.article.trailText?.let {
                        Text(it, style = MaterialTheme.typography.bodyLarge)
                    }
                    // TẠM THỜI: phần body (HTML) sẽ hiển thị ở bước sau
                    Text("(Nội dung bài viết sẽ hiển thị ở bước tiếp theo)")
                }
            }

            ArticleDetailUiState.NotFound -> {
                MessageWithAction(
                    message = "Bài viết không còn khả dụng.",
                    buttonText = "Quay lại danh sách",
                    onClick = onBackToList
                )
            }

            ArticleDetailUiState.NetworkError -> {
                MessageWithAction(
                    message = "Không có kết nối mạng.",
                    buttonText = "Thử lại",
                    onClick = { viewModel.load(articleId) }
                )
            }

            ArticleDetailUiState.Error -> {
                MessageWithAction(
                    message = "Có lỗi xảy ra.",
                    buttonText = "Thử lại",
                    onClick = { viewModel.load(articleId) }
                )
            }
        }
    }
}

@Composable
private fun BoxScope.MessageWithAction(
    message: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(message)
        Button(onClick = onClick) { Text(buttonText) }
    }
}