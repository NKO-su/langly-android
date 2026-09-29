package com.langly.langly_android.model

data class ArticlePageResponse(
    val articles: List<Article>,
    val nextCursor: Long?,
    val hasMore: Boolean
)