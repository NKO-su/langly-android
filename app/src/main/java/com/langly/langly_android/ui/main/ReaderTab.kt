package com.langly.langly_android.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.langly.langly_android.ui.article.ArticleDetailScreen
import com.langly.langly_android.ui.article.ArticleListScreen
import com.langly.langly_android.ui.article.ArticleViewModel
import com.langly.langly_android.ui.translate.WordPopupViewModel

@Composable
fun ReaderTab(
    articleId: Long?,
    onArticleClick: (Long) -> Unit,
    onBackToList: () -> Unit,
    articleViewModel: ArticleViewModel,
    popupViewModel: WordPopupViewModel,
    modifier: Modifier = Modifier
) {
    if (articleId == null) {
        ArticleListScreen(
            onArticleClick = onArticleClick,
            modifier = modifier,
            viewModel = articleViewModel
        )
    } else {
        ArticleDetailScreen(
            articleId = articleId,
            popupViewModel = popupViewModel,
            onBackToList = {
                articleViewModel.refresh()
                onBackToList()
            },
            modifier = modifier
        )
    }
}