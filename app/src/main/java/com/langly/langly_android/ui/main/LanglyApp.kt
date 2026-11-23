package com.langly.langly_android.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.langly.langly_android.network.AuthState
import com.langly.langly_android.network.TokenManager
import com.langly.langly_android.ui.article.ArticleViewModel
import com.langly.langly_android.ui.savedwords.SavedWordsScreen
import com.langly.langly_android.ui.translate.TranslateFab
import com.langly.langly_android.ui.translate.WordPopupViewModel
import kotlinx.coroutines.launch

@Composable
fun LanglyApp(onLogout: suspend () -> Unit) {
    val authFlow = remember { TokenManager.authState }
    val authState by authFlow.collectAsState(initial = AuthState.Loading)

    var selectedTab by rememberSaveable { mutableStateOf(MainTab.READ) }
    var selectedArticleId by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()

    val popupViewModel: WordPopupViewModel = viewModel()
    var bottomBarPadding by remember { mutableStateOf(0.dp) }

    LaunchedEffect(authState) {
        if (authState == AuthState.LoggedOut) {
            selectedArticleId = null
            selectedTab = MainTab.READ
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (authState == AuthState.LoggedIn &&
                    (selectedTab == MainTab.WORDS || selectedArticleId == null)
                ) {
                    MainTopBar(onLogout = { scope.launch { onLogout() } })
                }
            },
            bottomBar = {
                if (authState == AuthState.LoggedIn) {
                    MainBottomBar(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }
            }
        ) { innerPadding ->
            SideEffect { bottomBarPadding = innerPadding.calculateBottomPadding() }

            when (authState) {
                AuthState.Loading -> LoadingScreen(modifier = Modifier.padding(innerPadding))

                AuthState.LoggedIn -> LoggedInContent(
                    selectedTab = selectedTab,
                    selectedArticleId = selectedArticleId,
                    onSelectArticle = { selectedArticleId = it },
                    popupViewModel = popupViewModel,
                    innerPadding = innerPadding
                )

                AuthState.LoggedOut -> AuthScreens(modifier = Modifier.padding(innerPadding))
            }
        }

        if (authState == AuthState.LoggedIn) {
            TranslateFab(
                viewModel = popupViewModel,
                bottomPadding = bottomBarPadding
            )
        }
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun LoggedInContent(
    selectedTab: MainTab,
    selectedArticleId: Long?,
    onSelectArticle: (Long?) -> Unit,
    popupViewModel: WordPopupViewModel,
    innerPadding: PaddingValues
) {
    val articleViewModel: ArticleViewModel = viewModel()
    val saveableStateHolder = rememberSaveableStateHolder()

    BackHandler(enabled = selectedTab == MainTab.READ && selectedArticleId != null) {
        onSelectArticle(null)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (selectedTab) {
            MainTab.READ -> saveableStateHolder.SaveableStateProvider("tab_read") {
                ReaderTab(
                    articleId = selectedArticleId,
                    onArticleClick = { onSelectArticle(it) },
                    onBackToList = { onSelectArticle(null) },
                    articleViewModel = articleViewModel,
                    popupViewModel = popupViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            MainTab.WORDS -> saveableStateHolder.SaveableStateProvider("tab_words") {
                SavedWordsScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}