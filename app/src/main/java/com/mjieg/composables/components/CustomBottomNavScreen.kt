package com.mjieg.composables.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NavigationBarColor = Color.White
private val SelectedItemColor = Color(0xFFE8E8E8)
private val SelectedContentColor = Color(0xFF0878CE)
private val UnselectedContentColor = Color(0xFF707070)
private val CenterIconColor = Color(0xFF777777)

private val NavigationBarHeight = 80.dp
private val NavigationContainerHeight = 120.dp
private val CenterButtonSize = 80.dp

/**
 * A screen-sized example showing the custom bottom navigation bar.
 *
 * The selected index is intentionally owned by this sample screen so the preview/demo is
 * immediately interactive. Use [CustomBottomNavigationBar] when the state belongs to a parent.
 */
@Composable
fun CustomBottomNavScreen(
    modifier: Modifier = Modifier,
    onCenterClick: () -> Unit = {},
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavigationBarColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (selectedIndex == 0) "Speed Test" else "Advanced",
                color = if (selectedIndex == 0) SelectedContentColor else UnselectedContentColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        CustomBottomNavigationBar(
            selectedIndex = selectedIndex,
            onItemSelected = { selectedIndex = it },
            onCenterClick = onCenterClick,
            modifier = Modifier.navigationBarsPadding(),
        )
    }
}

/**
 * A two-item bottom navigation bar with a center floating action button and a matching notch.
 *
 * [selectedIndex] must be 0 for Speed Test or 1 for Advanced. The component is stateless so
 * callers can connect it to their own navigation state.
 */
@Composable
fun CustomBottomNavigationBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onCenterClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(NavigationContainerHeight),
    ) {
        val notchShape = CenterNotchShape(
            notchRadius = CenterButtonSize / 2 + 8.dp,
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(NavigationBarHeight)
                .shadow(
                    elevation = 3.dp,
                    shape = notchShape,
                    clip = false,
                )
                .clip(notchShape)
                .background(NavigationBarColor),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BottomNavigationItem(
                    title = "Speed Test",
                    icon = Icons.Outlined.Speed,
                    selected = selectedIndex == 0,
                    onClick = { onItemSelected(0) },
                    modifier = Modifier.weight(1f),
                )

                BottomNavigationItem(
                    title = "Advanced",
                    icon = Icons.Outlined.Settings,
                    selected = selectedIndex == 1,
                    onClick = { onItemSelected(1) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(CenterButtonSize)
                .shadow(elevation = 8.dp, shape = CircleShape, clip = false)
                .clip(CircleShape)
                .background(NavigationBarColor)
                .clickable(
                    role = Role.Button,
                    onClick = onCenterClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.SwapHoriz,
                contentDescription = "Switch mode",
                tint = CenterIconColor,
                modifier = Modifier.size(42.dp),
            )
        }
    }
}

@Composable
private fun BottomNavigationItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transition = updateTransition(
        targetState = selected,
        label = "$title selection",
    )
    val itemBackground by transition.animateColor(
        transitionSpec = { spring(stiffness = Spring.StiffnessMedium) },
        label = "$title background",
    ) { isSelected ->
        if (isSelected) SelectedItemColor else Color.Transparent
    }
    val contentColor by transition.animateColor(
        transitionSpec = { spring(stiffness = Spring.StiffnessMedium) },
        label = "$title content",
    ) { isSelected ->
        if (isSelected) SelectedContentColor else UnselectedContentColor
    }

    Column(
        modifier = modifier
            .height(NavigationBarHeight)
            .background(itemBackground)
            .clickable(
                role = Role.Tab,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(30.dp),
        )
        Text(
            text = title,
            color = contentColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

private class CenterNotchShape(
    private val notchRadius: Dp,
) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val radius = with(density) { notchRadius.toPx() }
        val centerX = size.width / 2f
        val safeRadius = radius.coerceAtMost(centerX - 1f)

        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(centerX - safeRadius, 0f)

            // Use the top edge as the diameter of a circle and trace its lower semicircle.
            arcTo(
                rect = Rect(
                    left = centerX - safeRadius,
                    top = 0f,
                    right = centerX + safeRadius,
                    bottom = safeRadius,
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false,
            )

            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        return Outline.Generic(path)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 240)
@Composable
private fun CustomBottomNavScreenPreview() {
    CustomBottomNavScreen()
}
