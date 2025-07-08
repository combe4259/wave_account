package com.example.accountbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatisticTopBar(
    modifier: Modifier = Modifier
) {
    val primary      = MaterialTheme.colorScheme.primary
    val statusInsets = WindowInsets.statusBars.asPaddingValues()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(primary)
            .height(56.dp + statusInsets.calculateTopPadding())// primary-coloured bar
            .padding(top = statusInsets.calculateTopPadding()),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "소비 내역 분석",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
        )
    }
}
