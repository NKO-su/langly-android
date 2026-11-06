package com.langly.langly_android.ui.savedwords

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SavedWordsScreen(
    modifier: Modifier = Modifier,
    viewModel: SavedWordsViewModel = viewModel()
) {
    val words by viewModel.words.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val list = words
            when {
                list == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                list.isEmpty() -> {
                    Text(
                        "nothing here",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(list, key = { it.id }) { word ->
                            Column {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = word.word,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (word.meaning.isNotBlank()) {
                                        Text(
                                            text = word.meaning,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }


        TextButton(
            onClick = viewModel::addSampleWord,
            modifier = Modifier.padding(8.dp)
        ) {
            Text("TEmp")
        }
    }
}