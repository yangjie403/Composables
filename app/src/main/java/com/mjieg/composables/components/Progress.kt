package com.mjieg.composables.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun CircularThickWavyProgressIndicatorSample() {
    var progress by remember { mutableFloatStateOf(0.1f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    )
    val thickStrokeWidth = with(LocalDensity.current) { 8.dp.toPx() }
    val thickStroke =
        remember(thickStrokeWidth) { Stroke(width = thickStrokeWidth, cap = StrokeCap.Round) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularWavyProgressIndicator(
            progress = { animatedProgress },
            // Thick size is slightly larger than the
            // WavyProgressIndicatorDefaults.CircularContainerSize default
            modifier = Modifier.size(80.dp), // Thick size is slightly larger than the default
            stroke = thickStroke,
            trackStroke = thickStroke,
        )
        Spacer(Modifier.requiredHeight(30.dp))
        Text("Set progress:")
        Slider(
            modifier = Modifier.width(300.dp),
            value = progress,
            valueRange = 0f..1f,
            onValueChange = { progress = it },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun CircularWavyProgressIndicatorSample() {
    var progress by remember { mutableFloatStateOf(0.1f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularWavyProgressIndicator(progress = { animatedProgress })
        Spacer(Modifier.requiredHeight(30.dp))
        Text("Set progress:")
        Slider(
            modifier = Modifier.width(300.dp),
            value = progress,
            valueRange = 0f..1f,
            onValueChange = { progress = it },
        )
    }
}

@Composable
@Preview
fun ProgressPreviewLayout() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularWavyProgressIndicatorSample()
        Spacer(Modifier.requiredHeight(30.dp))
        CircularThickWavyProgressIndicatorSample()
    }
}