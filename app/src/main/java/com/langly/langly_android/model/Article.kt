package com.langly.langly_android.model

data class Article(
    val id: Long,
    val title: String,
    val description: String?,
    val link: String,
    val pubDate: String?,
    val source: String?
)