package com.langly.langly_android.ui.article

import com.langly.langly_android.model.ArticleDetail

sealed interface ArticleDetailUiState {
    data object Loading : ArticleDetailUiState
    data class Success(val article: ArticleDetail) : ArticleDetailUiState
    data object NotFound : ArticleDetailUiState
    data object NetworkError : ArticleDetailUiState
    data object Error : ArticleDetailUiState
}