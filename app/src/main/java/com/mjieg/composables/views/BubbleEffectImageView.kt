package com.mjieg.composables.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.sin
import kotlin.random.Random

class BubbleEffectImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    // 最大并发气泡群数
    private val MAX_GROUPS = 10
    // 气泡存活时间 (3.5秒)
    private val DURATION = 3500L

    // 屏幕密度适配
    private val density = context.resources.displayMetrics.density
    
    // 浮力加速度 (模拟气泡越往上跑越快)
    private val BUOYANCY = 150f * density 

    // 活跃的气泡群队列
    private val activeGroups = ArrayDeque<BubbleGroup>()

    // 气泡描边画笔
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
        color = Color.WHITE
    }

    // 气泡内部填充画笔（半透明）
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    // 记录点击位置
    private var lastTouchX: Float = -1f
    private var lastTouchY: Float = -1f

    init {
        isClickable = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            lastTouchX = event.x
            lastTouchY = event.y
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        val handled = super.performClick()
        val startX = if (lastTouchX >= 0) lastTouchX else width / 2f
        val startY = if (lastTouchY >= 0) lastTouchY else height / 2f
        
        triggerEffect(startX, startY)
        return handled
    }

    private fun triggerEffect(x: Float, y: Float) {
        if (activeGroups.size >= MAX_GROUPS) {
            activeGroups.removeFirst()
        }
        activeGroups.addLast(BubbleGroup(x, y, System.currentTimeMillis()))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (activeGroups.isEmpty()) return

        val currentTime = System.currentTimeMillis()
        val iterator = activeGroups.iterator()
        var needsNextFrame = false

        while (iterator.hasNext()) {
            val group = iterator.next()
            val isFinished = group.updateAndDraw(canvas, currentTime, strokePaint, fillPaint)
            
            if (isFinished) {
                iterator.remove()
            } else {
                needsNextFrame = true
            }
        }

        if (needsNextFrame) {
            invalidate()
        }
    }

    // --- 气泡模型与逻辑 ---

    private inner class Bubble(
        val startX: Float,
        val startY: Float,
        val speedY: Float,       // 初始向上的速度
        val radius: Float,       // 气泡半径
        val amplitude: Float,    // 摇摆幅度 (X轴正弦波振幅)
        val frequency: Float,    // 摇摆频率
        val phaseOffset: Float   // 相位偏移，确保每个气泡摇摆步调不一致
    )

    private inner class BubbleGroup(
        val startX: Float,
        val startY: Float,
        val startTime: Long
    ) {
        private val bubbles = mutableListOf<Bubble>()

        init {
            // 一次产生 6 ~ 12 个气泡
            val count = Random.nextInt(6, 13)

            for (i in 0 until count) {
                // 气泡向上的初始基础速度 (50dp/s ~ 150dp/s)
                val speedY = (Random.nextFloat() * 100f + 50f) * density
                
                // 气泡大小 (8dp ~ 18dp)
                val radius = (Random.nextFloat() * 10f + 8f) * density
                
                // 左右摇摆的幅度 (10dp ~ 50dp)
                val amplitude = (Random.nextFloat() * 40f + 10f) * density
                
                // 摇摆频率 (控制S型曲线的疏密)
                val frequency = Random.nextFloat() * 3f + 2f
                
                // 随机初始相位 (0 ~ 2π)，让气泡一出来左右摆动方向就不一样
                val phaseOffset = Random.nextFloat() * (2 * Math.PI).toFloat()

                // 让气泡在点击位置附近有一个极小的随机起始X偏移，不至于全部挤在一条直线上
                val initOffsetX = startX + (Random.nextFloat() - 0.5f) * 10f * density

                bubbles.add(
                    Bubble(initOffsetX, startY, speedY, radius, amplitude, frequency, phaseOffset)
                )
            }
        }

        fun updateAndDraw(canvas: Canvas, currentTime: Long, strokePaint: Paint, fillPaint: Paint): Boolean {
            val elapsed = currentTime - startTime
            if (elapsed >= DURATION) {
                return true
            }

            val t = elapsed / 1000f // 秒
            val progress = elapsed.toFloat() / DURATION
            
            // 气泡随着上升，透明度逐渐变浅，最后消失
            // 使用非线性衰减(如 progress 的平方)，让气泡在最后阶段才快速变透明
            val alphaFactor = 1f - progress * progress 
            
            // 内部填充设置 30% 左右的不透明度 (max: 255 * 0.3 = 76)
            fillPaint.alpha = (76 * alphaFactor).toInt()
            // 描边设置更高一点的不透明度 (max: 255 * 0.8 = 204)
            strokePaint.alpha = (204 * alphaFactor).toInt()

            for (b in bubbles) {
                // Y轴运动：向上匀速 + 浮力向上加速。 注意 Android 坐标系 Y 轴向下是正，所以是减号
                val currentY = b.startY - (b.speedY * t) - (0.5f * BUOYANCY * t * t)
                
                // X轴运动：基于起始X坐标，叠加一个随时间变化的正弦波 (sin)，实现S形曲线摇摆
                val currentX = b.startX + b.amplitude * sin(b.frequency * t + b.phaseOffset)

                // 气泡在上升过程中稍微变大一点点 (模拟水压变小气泡膨胀)，最多放大 20%
                val currentRadius = b.radius * (1f + 0.2f * progress)

                // 先画半透明内部
                canvas.drawCircle(currentX, currentY, currentRadius, fillPaint)
                // 再画清晰描边，这样看起来就像晶莹剔透的水泡
                canvas.drawCircle(currentX, currentY, currentRadius, strokePaint)
            }
            return false
        }
    }
}