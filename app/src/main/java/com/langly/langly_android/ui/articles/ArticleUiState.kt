package com.langly.langly_android.ui.articles

import com.langly.langly_android.model.Article

data class ArticleUiState(
    val articles: List<Article> = emptyList(),
    val nextCursor: Long? = null,
    val hasMore: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)