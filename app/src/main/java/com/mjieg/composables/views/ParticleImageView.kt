package com.mjieg.composables.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class ParticleImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    val density = resources.displayMetrics.density

    // 预分配粒子对象
    class Particle {
        var velocityX: Float = 0f
        var velocityY: Float = 0f

        // 绘制圆圈专用
        var radius: Float = 0f
        var color: Int = 0

        // 绘制 Bitmap 专用
        var bitmap: Bitmap? = null
        var targetHalfSize: Float = 0f // 随机生成的贴纸宽高的一半
        val dstRect = RectF()          // 预分配 RectF，用于在 onDraw 中零分配缩放绘制
    }

    // 预分配特效对象
    class Effect {
        var startX: Float = 0f
        var startY: Float = 0f
        var startTime: Long = 0L
        var isActive: Boolean = false
        var particleCount: Int = 0
        val particles = Array(16) { Particle() }
    }

    private val maxEffects = 6
    private val effects = Array(maxEffects) { Effect() }
    private var effectIndex = 0

    // 画笔与临时变量
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val hsvTemp = floatArrayOf(0f, 0.8f, 1f)

    private val duration = 3000f // 动画时长 3s

    // ===== 新增：外部传入的 Bitmap 相关 =====
    private var particleBitmaps: Array<Bitmap>? = null
    private var currentBitmapIndex: Int = 0 // 全局索引，保证跨 Effect 循环提取

    // DecelerateInterpolator (减速插值器) 非常适合爆炸粒子：一开始速度极快，然后慢慢停下
    private val interpolator = DecelerateInterpolator(2f)

    /**
     * 供外部调用的方法：设置用于贴纸特效的 Bitmap 数组
     * 传入 null 或空数组则自动回退为绘制彩色小圆圈
     */
    fun setParticleBitmaps(bitmaps: Array<Bitmap>?) {
        this.particleBitmaps = bitmaps
        // 如果更换了数组，重置索引，防止数组越界
        this.currentBitmapIndex = 0
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            triggerEffect(event.x, event.y)
        }
        return super.dispatchTouchEvent(event)
    }

    private fun triggerEffect(x: Float, y: Float) {
        val effect = effects[effectIndex]
        effect.isActive = true
        effect.startTime = AnimationUtils.currentAnimationTimeMillis()
        effect.startX = x
        effect.startY = y
        effect.particleCount = Random.nextInt(8, 17)

        val angleStep = (2 * Math.PI / effect.particleCount).toFloat()
        val bitmaps = particleBitmaps
        val hasBitmaps = !bitmaps.isNullOrEmpty()

        for (i in 0 until effect.particleCount) {
            val p = effect.particles[i]
            val baseAngle = i * angleStep
            val angleOffset = (Random.nextFloat() - 0.5f) * (angleStep * 0.8f)
            val angle = baseAngle + angleOffset
            // 扩散距离 100dp ~ 200dp
            val distance = (Random.nextFloat() * 100f + 100f) * density

            p.velocityX = cos(angle) * distance
            p.velocityY = sin(angle) * distance

            if (hasBitmaps) {
                // 有贴纸：循环取图
                p.bitmap = bitmaps[currentBitmapIndex % bitmaps.size]

                // 【核心修改】随机生成目标尺寸，忽略原图大小 (例如宽/高在 20dp 到 50dp 之间)
                val targetSizePx = (Random.nextFloat() * 30f + 20f) * density
                p.targetHalfSize = targetSizePx / 2f

                currentBitmapIndex = (currentBitmapIndex + 1) % bitmaps.size
            } else {
                p.bitmap = null
                // 圆圈大小 5dp ~ 15dp
                p.radius = (Random.nextFloat() * 10f + 5f) * density
                hsvTemp[0] = Random.nextFloat() * 360f
                p.color = Color.HSVToColor(hsvTemp)
            }
        }

        effectIndex = (effectIndex + 1) % maxEffects
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var hasActiveEffects = false
        val currentTime = AnimationUtils.currentAnimationTimeMillis()

        for (i in effects.indices) {
            val effect = effects[i]
            if (!effect.isActive) continue

            val elapsed = currentTime - effect.startTime
            if (elapsed >= duration) {
                effect.isActive = false
                continue
            }

            hasActiveEffects = true
            val progress = elapsed / duration
            val alpha = (255 * (1f - progress)).toInt() // 计算当前透明度
            val easedProgress = interpolator.getInterpolation(progress)

            for (j in 0 until effect.particleCount) {
                val p = effect.particles[j]
                val currentX = effect.startX + p.velocityX * easedProgress
                val currentY = effect.startY + p.velocityY * easedProgress

                val bmp = p.bitmap
                if (bmp != null) {
                    // 绘制小图标贴纸
                    paint.alpha = alpha // drawBitmap 可以通过 paint 直接设置透明度
                    p.dstRect.set(
                        currentX - p.targetHalfSize,
                        currentY - p.targetHalfSize,
                        currentX + p.targetHalfSize,
                        currentY + p.targetHalfSize
                    )
                    canvas.drawBitmap(bmp, null, p.dstRect, paint)
                } else {
                    // 绘制小圆圈
                    paint.color = p.color
                    paint.alpha = alpha // 注意：设置 color 会覆盖 alpha，所以 alpha 必须在 color 之后设置
                    canvas.drawCircle(currentX, currentY, p.radius, paint)
                }
            }
        }

        if (hasActiveEffects) {
            invalidate()
        }
    }
}