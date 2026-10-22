package com.langly.langly_android.model

data class ArticleDetail(
    val id: Long,
    val title: String,
    val trailText: String?,
    val thumbnail: String?,
    val publishedAt: String?,
    val webUrl: String,
    val byline: String?,
    val body: String?
)