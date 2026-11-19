package com.langly.langly_android.ui.translate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val BUTTON_SIZE = 56.dp

@Composable
internal fun FabButtonContent(onClick: () -> Unit) {
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    Column(
        modifier = Modifier
            .size(BUTTON_SIZE)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "文A",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
        Text(
            text = "Dịch",
            fontSize = 11.sp,
            color = contentColor
        )
    }
}