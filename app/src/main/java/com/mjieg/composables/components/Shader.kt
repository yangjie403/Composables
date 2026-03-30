package com.mjieg.composables.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mjieg.composables.R

// AGSL 脚本
// AGSL 脚本：半程渐变模糊
const val HALF_GRADIENT_BLUR_SHADER = """
    uniform shader composable;
    uniform float2 size;

    half4 main(float2 fragCoord) {
        // 1. 归一化 Y 坐标 (0.0 到 1.0)
        float yPercent = fragCoord.y / size.y;
        
        // 2. 核心逻辑：定义从哪里开始模糊
        // 使用 clamp 函数：
        // 如果 yPercent < 0.4，结果为 0 (不模糊)
        // 如果 yPercent > 0.4，结果从 0 线性增加到 1.0
        // 公式：(当前百分比 - 起始点) / (结束点 - 起始点)
        float progress = clamp((yPercent - 0.4) / 0.4, 0.0, 1.0);
        
        // 设定最大模糊半径 (像素级)
        float maxBlur = 10.0;
        float currentRadius = progress * maxBlur;

        // 如果在不模糊区域，直接返回原色，节省计算资源
        if (currentRadius <= 0.1) {
            return composable.eval(fragCoord);
        }

        half4 color = half4(0.0);
        float totalWeight = 0.0;

        // 3. 采样循环 (简单的高斯近似采样)
        // 注意：采样步长随 currentRadius 动态变化
        for (float x = -2.0; x <= 2.0; x += 1.0) {
            for (float y = -2.0; y <= 2.0; y += 1.0) {
                // 根据计算出的半径进行偏移
                float2 offset = float2(x, y) * (currentRadius / 2.0);
                color += composable.eval(fragCoord + offset);
                totalWeight += 1.0;
            }
        }
        return color / totalWeight;
    }
"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun BottomGradientBlurImage() {
    val shader = remember { RuntimeShader(HALF_GRADIENT_BLUR_SHADER) }

    Box(
        modifier = Modifier
            .size(300.dp, 200.dp)
            .onSizeChanged { size ->
                // 更新 Shader 内部的尺寸参数
                shader.setFloatUniform("size", size.width.toFloat(), size.height.toFloat())
            }
            .clip(RoundedCornerShape(8.dp))
            .graphicsLayer {
                // 应用着色器效果
                renderEffect = RenderEffect.createRuntimeShaderEffect(
                    shader, "composable"
                ).asComposeRenderEffect()
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_bn_6),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }
}