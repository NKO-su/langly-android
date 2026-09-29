package com.langly.langly_android.network

import com.langly.langly_android.model.ArticlePageResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ArticleApi {

    @GET("api/articles")
    suspend fun getArticles(
        @Query("cursor") cursor: Long?,
        @Query("size") size: Int = 10
    ): ArticlePageResponse
}