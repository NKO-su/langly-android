package com.langly.langly_android.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.langly.langly_android.ui.login.LoginScreen
import com.langly.langly_android.ui.register.RegisterScreen

@Composable
fun AuthScreens(modifier: Modifier = Modifier) {
    var showRegister by remember { mutableStateOf(false) }

    BackHandler(enabled = showRegister) {
        showRegister = false
    }

    if (showRegister) {
        RegisterScreen(
            modifier = modifier,
            onBackToLogin = { showRegister = false }
        )
    } else {
        LoginScreen(
            modifier = modifier,
            onLoginSuccess = { },
            onGoToRegister = { showRegister = true }
        )
    }
}