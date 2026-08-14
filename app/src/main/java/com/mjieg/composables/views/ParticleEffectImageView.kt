package com.mjieg.composables.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.core.graphics.toColorInt

class ParticleEffectImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    // 最大并发特效数
    private val MAX_GROUPS = 8
    // 动画时长 (毫秒)
    private val DURATION = 3000L

    // 屏幕像素密度，用于适配不同屏幕
    private val density = context.resources.displayMetrics.density
    // 重力加速度 (适配 dp)
    private val GRAVITY = 400f * density

    // 活跃的粒子群队列
    private val activeGroups = ArrayDeque<ParticleGroup>()

    // 画笔
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // 记录最后一次点击的坐标
    private var lastTouchX: Float = -1f
    private var lastTouchY: Float = -1f

    init {
        // 确保 View 是可点击的
        isClickable = true
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            triggerEffect(event.x, event.y)
        }
        return super.dispatchTouchEvent(event)
    }

    private fun triggerEffect(x: Float, y: Float) {
        // 如果当前特效已经达到 6 个，移除最早的那一个
        if (activeGroups.size >= MAX_GROUPS) {
            activeGroups.removeFirst()
        }

        // 添加新的粒子组，并触发重绘
        activeGroups.addLast(ParticleGroup(x, y, System.currentTimeMillis()))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        // 先绘制原本的 ImageView 内容（即你的主图片）
        super.onDraw(canvas)

        if (activeGroups.isEmpty()) return

        val currentTime = System.currentTimeMillis()
        val iterator = activeGroups.iterator()
        var needsNextFrame = false

        // 遍历所有正在运行的特效组
        while (iterator.hasNext()) {
            val group = iterator.next()
            val isFinished = group.updateAndDraw(canvas, currentTime, paint)

            if (isFinished) {
                iterator.remove() // 超过 3s，移除该粒子群
            } else {
                needsNextFrame = true // 仍有未结束的特效，需要请求下一帧
            }
        }

        // 持续驱动动画
        if (needsNextFrame) {
            invalidate()
        }
    }

    // --- 内部数据类与逻辑 ---

    // 单个粒子模型
    private class Particle(
        val startX: Float,
        val startY: Float,
        val vx: Float,      // X轴速度
        val vy: Float,      // Y轴速度
        val radius: Float,  // 粒子绘制大小
        val color: Int
        // var bitmap: Bitmap? = null // TODO: 后期换成贴纸时可放开此参数
    )

    // 单次点击产生的一组粒子
    private inner class ParticleGroup(
        val startX: Float,
        val startY: Float,
        val startTime: Long
    ) {
        private val particles = mutableListOf<Particle>()

        // 预设一些好看的粒子颜色
        private val colorPalette = intArrayOf(
            "#FF5252".toColorInt(), "#FF4081".toColorInt(),
            "#E040FB".toColorInt(), "#7C4DFF".toColorInt(),
            "#536DFE".toColorInt(), "#448AFF".toColorInt(),
            "#40C4FF".toColorInt(), "#18FFFF".toColorInt(),
            "#64FFDA".toColorInt(), "#69F0AE".toColorInt(),
            "#EEFF41".toColorInt(), "#FFD740".toColorInt()
        )

        init {
            // 生成 8 - 16 个粒子
            val count = Random.nextInt(8, 17)

            for (i in 0 until count) {
                // 角度分配：0 到 -180 度。使用数学弧度表示 (0 到 -PI)
                // 为了完全均匀分布，将 180 度划分为 count - 1 份
                val angle = if (count > 1) {
                    -Math.PI * (i.toDouble() / (count - 1))
                } else {
                    -Math.PI / 2 // 如果只有一个，直接朝正上方
                }

                // 添加极小的随机偏移让轨迹显得更自然，而不是死板的半圆
                val angleJitter = (Random.nextFloat() - 0.5) * 0.15
                val finalAngle = angle + angleJitter

                // 随机初始速度 (300dp/s ~ 600dp/s)
                val baseSpeed = 50f * density
                val speed = baseSpeed + Random.nextFloat() * (100f * density)

                // 极坐标转直角坐标计算初始速度向量
                val vx = (speed * cos(finalAngle)).toFloat()
                val vy = (speed * sin(finalAngle)).toFloat()

                // 随机半径 (4dp ~ 12dp)
                val radius = (Random.nextFloat() * 8f + 4f) * density
                val color = colorPalette[Random.nextInt(colorPalette.size)]

                particles.add(Particle(startX, startY, vx, vy, radius, color))
            }
        }

        /**
         * 更新物理状态并绘制
         * @return 是否已结束动画
         */
        fun updateAndDraw(canvas: Canvas, currentTime: Long, paint: Paint): Boolean {
            val elapsed = currentTime - startTime
            if (elapsed >= DURATION) {
                return true // 已经超过 3秒
            }

            // 经过的时间，换算成秒 (s) 作为物理计算的单位
            val t = elapsed / 1000f

            // 计算透明度衰减：从 1 衰减到 0
            val alphaProgress = 1f - (elapsed.toFloat() / DURATION)
            val currentAlpha = (255 * alphaProgress).toInt().coerceIn(0, 255)

            for (p in particles) {
                // 物理运动公式
                // X轴：匀速直线运动 (x = v * t)
                val currentX = p.startX + p.vx * t
                // Y轴：抛体运动 (y = v * t + 1/2 * g * t^2)
                val currentY = p.startY + p.vy * t + 0.5f * GRAVITY * t * t

                paint.color = p.color
                paint.alpha = currentAlpha

                // 【第一版】绘制形状（圆形）
                canvas.drawCircle(currentX, currentY, p.radius, paint)

                /*
                 * 【后期预留】如果要换成贴纸(Bitmap)，只需把上面的 drawCircle 替换成类似：
                 * if (p.bitmap != null) {
                 *     val left = currentX - p.bitmap.width / 2f
                 *     val top = currentY - p.bitmap.height / 2f
                 *     canvas.drawBitmap(p.bitmap, left, top, paint)
                 * }
                 */
            }
            return false // 动画仍在进行中
        }
    }
}