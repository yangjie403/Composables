package com.mjieg.composables.components.parallax

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mjieg.composables.components.parallax.model.ContainerSettings
import com.mjieg.composables.components.parallax.model.ParallaxOrientation
import com.mjieg.composables.components.parallax.model.SensorData
import com.mjieg.composables.components.parallax.sensor.SensorDataManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val INITIAL_VERTICAL_OFFSET = 0.5f
private const val TAG = "ParallaxView"

@Composable
fun ParallaxView(
    modifier: Modifier = Modifier,
    backgroundContent: @Composable (() -> Unit)? = null,
    middleContent: @Composable (() -> Unit)? = null,
    foregroundContent: @Composable (() -> Unit)? = null,
    backgroundContainerSettings: ContainerSettings = ContainerSettings(),
    middleContainerSettings: ContainerSettings = ContainerSettings(),
    foregroundContainerSettings: ContainerSettings = ContainerSettings(),
    movementIntensityMultiplier: Int = 1,
    verticalOffsetLimit: Float = 0.5f,
    horizontalOffsetLimit: Float = 0.5f,
    orientation: ParallaxOrientation = ParallaxOrientation.FULL
) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val data = getSensorData(context, configuration)

    ParallaxViewContent(
        data = data,
        backgroundContent = backgroundContent,
        middleContent = middleContent,
        foregroundContent = foregroundContent,
        modifier = modifier,
        depthMultiplier = movementIntensityMultiplier,
        backgroundScaleMultiplier = backgroundContainerSettings.scale,
        middleScaleMultiplier = middleContainerSettings.scale,
        foregroundScaleMultiplier = foregroundContainerSettings.scale,
        orientation = orientation,
        horizontalLimit = horizontalOffsetLimit,
        verticalLimit = verticalOffsetLimit,
        backgroundAlignment = backgroundContainerSettings.alignment,
        middleAlignment = middleContainerSettings.alignment,
        foregroundAlignment = foregroundContainerSettings.alignment
    )
}

@Composable
private fun ParallaxViewContent(
    data: SensorData,
    modifier: Modifier,
    backgroundContent: @Composable (() -> Unit)?,
    middleContent: @Composable (() -> Unit)?,
    foregroundContent: @Composable (() -> Unit)?,
    backgroundAlignment: Alignment,
    middleAlignment: Alignment,
    foregroundAlignment: Alignment,
    backgroundScaleMultiplier: Float,
    middleScaleMultiplier: Float,
    foregroundScaleMultiplier: Float,
    depthMultiplier: Int,
    horizontalLimit: Float,
    verticalLimit: Float,
    orientation: ParallaxOrientation
) {
    var roll = data.roll.coerceIn(getRange(horizontalLimit))
    var pitch = data.pitch.plus(0.5f).coerceIn(getRange(verticalLimit))
    when (orientation) {
        ParallaxOrientation.HORIZONTAL -> pitch = 0f
        ParallaxOrientation.VERTICAL -> roll = 0f
        else -> Unit
    }

    var backgroundSize by remember {
        mutableStateOf(IntSize(0, 0))
    }
    var backgroundRemainWidth by remember {
        mutableIntStateOf(0)
    }
    var backgroundRemainHeight by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(backgroundSize) {
        if (backgroundSize.width != 0 && backgroundSize.height != 0) {
            backgroundRemainWidth =
                ((backgroundSize.width * (backgroundScaleMultiplier.coerceAtLeast(minimumValue = 1f) - 1))
                    .div(2f)).toInt()
            backgroundRemainHeight =
                ((backgroundSize.height * (backgroundScaleMultiplier.coerceAtLeast(minimumValue = 1f) - 1))
                    .div(2)).toInt()
        }
    }

    backgroundContent?.let { background ->
        Box(modifier = Modifier) {
            Box(
                modifier = Modifier
                    .scale(backgroundScaleMultiplier)
                    .onSizeChanged {
                        backgroundSize = it
                    }
                    .offset {
                        IntOffset(
                            x = (backgroundRemainWidth * roll / 1.5f).toInt(),
                            y = (backgroundRemainHeight * pitch / 1.5f).toInt()
                        )
                    }
                    .align(backgroundAlignment),
                content = { background() }
            )
        }
    }
}

private fun getRange(value: Float): ClosedFloatingPointRange<Float> {
    val transformedValue = value * 1.5f
    return -transformedValue..transformedValue
}

@Composable
private fun getSensorData(context: Context, configuration: Configuration): SensorData {
    val scope = rememberCoroutineScope()
    var data by remember { mutableStateOf(SensorData()) }
    val deviceOrientation = configuration.orientation

    DisposableEffect(deviceOrientation) {
        val dataManager = SensorDataManager(context)
        dataManager.init(deviceOrientation)
        val job = scope.launch {
            dataManager.data
                .receiveAsFlow()
                .onEach {
                    data = it
                }
                .collect()
        }

        onDispose {
            dataManager.cancel()
            job.cancel()
        }
    }
    return data
}