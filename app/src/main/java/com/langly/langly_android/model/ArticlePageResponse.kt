package com.langly.langly_android.model

data class ArticlePageResponse(
    val articles: List<ArticleSummary>,
    val nextCursor: Long?,
    val hasMore: Boolean
)