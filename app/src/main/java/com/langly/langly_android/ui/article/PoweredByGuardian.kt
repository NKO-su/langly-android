package com.langly.langly_android.ui.article

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.langly.langly_android.R

@Composable
fun PoweredByGuardian(modifier: Modifier = Modifier) {
    val isLightBackground = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val logo = if (isLightBackground) {
        R.drawable.guardian_powered_black
    } else {
        R.drawable.guardian_powered_rev
    }

    Image(
        painter = painterResource(id = logo),
        contentDescription = "Powered by The Guardian",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .width(120.dp)
            .aspectRatio(140f / 45f)
    )
}