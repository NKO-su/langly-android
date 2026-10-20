package com.langly.langly_android.ui.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.langly.langly_android.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ArticleDetailViewModel : ViewModel() {

    private val api = RetrofitInstance.articleApi

    private val _state = MutableStateFlow<ArticleDetailUiState>(ArticleDetailUiState.Loading)
    val state: StateFlow<ArticleDetailUiState> = _state.asStateFlow()

    fun load(id: Long) {
        _state.value = ArticleDetailUiState.Loading
        viewModelScope.launch {
            _state.value = try {
                ArticleDetailUiState.Success(api.getArticle(id))
            } catch (e: HttpException) {
                if (e.code() == 404) ArticleDetailUiState.NotFound
                else ArticleDetailUiState.Error
            } catch (e: IOException) {
                ArticleDetailUiState.NetworkError
            }
        }
    }
}