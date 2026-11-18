package com.rafalniski.scalableapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.rafalniski.scalableapp.R

@Composable
fun CommitActivityBar(monthLabel: String, count: Int, maxCount: Int, modifier: Modifier = Modifier) {
    val heightFraction by animateFloatAsState(if (maxCount > 0) count.toFloat() / maxCount else 0f)
    Column(modifier.semantics(mergeDescendants = true) {}, horizontalAlignment = Alignment.CenterHorizontally) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(pluralStringResource(R.plurals.commit_count, count, count))
            Box(
                Modifier
                    .width(48.dp)
                    .fillMaxHeight(heightFraction)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        Text(monthLabel)
    }
}
