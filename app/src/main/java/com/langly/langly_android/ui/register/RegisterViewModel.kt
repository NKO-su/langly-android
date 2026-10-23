package com.langly.langly_android.ui.register

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.langly.langly_android.model.RegisterRequest
import com.langly.langly_android.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String) {
        val trimmedEmail = email.trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _uiState.value = RegisterUiState(errorMessage = "Email không đúng định dạng")
            return
        }
        if (password.length < 8) {
            _uiState.value = RegisterUiState(errorMessage = "Mật khẩu phải có ít nhất 8 ký tự")
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            try {
                val response = RetrofitInstance.authApi.register(
                    RegisterRequest(trimmedEmail, password)
                )
                _uiState.value = if (response.isSuccessful) {
                    RegisterUiState(isSuccess = true)
                } else {
                    RegisterUiState(
                        errorMessage = "Đăng ký không thành công. Email có thể đã được dùng " +
                                "hoặc đang chờ xác nhận. Hãy kiểm tra email hoặc thử đăng nhập."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState(
                    errorMessage = "Không kết nối được máy chủ. Hãy kiểm tra mạng và thử lại."
                )
            }
        }
    }

    fun reset() {
        _uiState.value = RegisterUiState()
    }
}