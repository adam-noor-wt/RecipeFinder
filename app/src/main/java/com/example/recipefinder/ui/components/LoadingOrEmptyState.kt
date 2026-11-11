package com.example.recipefinder.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.recipefinder.ui.theme.Primary

/** Spinner while recipes are loading, otherwise the given empty-state message. */
@Composable
fun LoadingOrEmptyState(isLoaded: Boolean, emptyMessage: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoaded) Text(emptyMessage, color = Color.Gray)
        else CircularProgressIndicator(color = Primary, modifier = Modifier.size(48.dp))
    }
}
