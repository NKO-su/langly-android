package com.langly.langly_android.ui.article

import com.langly.langly_android.model.ArticleSummary

data class ArticleUiState(
    val articles: List<ArticleSummary> = emptyList(),
    val nextCursor: Long? = null,
    val hasMore: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)