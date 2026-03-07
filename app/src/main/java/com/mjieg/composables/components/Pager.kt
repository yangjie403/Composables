package com.mjieg.composables.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.mjieg.composables.R
import kotlin.math.absoluteValue

private data class BannerItem(
    val id: Int,
    val imageResId: Int,
    val description: String
)

@Composable
fun BannerPage() {
    val items = listOf(
        BannerItem(1, R.drawable.bg_bn_1, "bg_1"),
        BannerItem(2, R.drawable.bg_bn_2, "bg_2"),
        BannerItem(3, R.drawable.bg_bn_3, "bg_3"),
        BannerItem(4, R.drawable.bg_bn_4, "bg_4"),
        BannerItem(5, R.drawable.bg_bn_5, "bg_5"),
        BannerItem(6, R.drawable.bg_bn_6, "bg_6"),
        BannerItem(7, R.drawable.bg_bn_7, "bg_7"),
        BannerItem(8, R.drawable.bg_bn_8, "bg_8"),
        BannerItem(9, R.drawable.bg_bn_9, "bg_9"),
        BannerItem(10, R.drawable.bg_bn_10, "bg_10"),
        BannerItem(11, R.drawable.bg_bn_11, "bg_11")
    )
    val contentHorizontalSpacing = 40.dp
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val imageRatio = 150.dp.div(281.dp)
    val imageWidth = screenWidth - contentHorizontalSpacing.times(2)
    val imageHeight = imageWidth.times(imageRatio)
    val pagerState = rememberPagerState(
        pageCount = {
            items.size
        }
    )


    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight),
            beyondViewportPageCount = 1,
            pageSpacing = contentHorizontalSpacing.div(2),
            contentPadding = PaddingValues(horizontal = contentHorizontalSpacing)
        ) { page ->
            val item = items[page]
            val pageOffset =
                ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
            Card(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .graphicsLayer {
                        val startAlpha = 0.4f
                        this.alpha = lerp(
                            start = startAlpha,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        this.scaleY = lerp(
                            start = 0.8f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                    }
            ) {
                Image(
                    modifier = Modifier.size(width = imageWidth, height = imageHeight),
                    painter = painterResource(item.imageResId),
                    contentDescription = item.description,
                    contentScale = ContentScale.FillBounds
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(
                            height = 6.dp,
                            width = if (pagerState.currentPageOffsetFraction > 0) {
                                if (index == pagerState.currentPage) {
                                    lerp(
                                        start = 6f,
                                        stop = 16f,
                                        fraction = 1 - pagerState.currentPageOffsetFraction
                                    ).dp
                                } else if (index == pagerState.currentPage + 1) {
                                    lerp(
                                        start = 6f,
                                        stop = 16f,
                                        fraction = pagerState.currentPageOffsetFraction
                                    ).dp
                                } else {
                                    6.dp
                                }
                            } else {
                                if (index == pagerState.currentPage) {
                                    lerp(
                                        start = 6f,
                                        stop = 16f,
                                        fraction = 1 + pagerState.currentPageOffsetFraction
                                    ).dp
                                } else if (index == pagerState.currentPage - 1) {
                                    lerp(
                                        start = 6f,
                                        stop = 16f,
                                        fraction = pagerState.currentPageOffsetFraction.absoluteValue
                                    ).dp
                                } else {
                                    6.dp
                                }
                            }
                        )
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (index == pagerState.currentPage)
                                Color.Blue.copy(0.8f)
                            else
                                Color.Blue.copy(alpha = 0.6f)
                        )
                )
            }
        }
    }
}