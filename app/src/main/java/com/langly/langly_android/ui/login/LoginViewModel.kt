package com.langly.langly_android.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.langly.langly_android.model.LoginRequest
import com.langly.langly_android.network.RetrofitInstance
import com.langly.langly_android.network.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState(errorMessage = "Vui lòng nhập email và mật khẩu")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            try {
                val response = RetrofitInstance.authApi.login(LoginRequest(email, password))
                TokenManager.saveTokens(response.accessToken, response.refreshToken)
                _uiState.value = LoginUiState(isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = LoginUiState(errorMessage = "Đăng nhập thất bại: ${e.message}")
            }
        }
    }
}