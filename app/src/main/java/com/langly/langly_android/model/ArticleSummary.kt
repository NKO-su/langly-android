package com.langly.langly_android.model

data class ArticleSummary(
    val id: Long,
    val title: String,
    val trailText: String?,
    val thumbnail: String?,
    val publishedAt: String?,
    val webUrl: String
)