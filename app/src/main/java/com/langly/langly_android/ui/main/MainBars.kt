package com.langly.langly_android.ui.main

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(onLogout: () -> Unit) {
    TopAppBar(
        title = { Text("Ourilang") },
        actions = {
            TextButton(onClick = onLogout) {
                Text("Đăng xuất")
            }
        }
    )
}

@Composable
fun MainBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == MainTab.READ,
            onClick = { onTabSelected(MainTab.READ) },
            icon = { Text("DOC") },
            label = { Text("Đọc") }
        )
        NavigationBarItem(
            selected = selectedTab == MainTab.WORDS,
            onClick = { onTabSelected(MainTab.WORDS) },
            icon = { Text("TUDALUC") },
            label = { Text("Từ đã lưu") }
        )
    }
}