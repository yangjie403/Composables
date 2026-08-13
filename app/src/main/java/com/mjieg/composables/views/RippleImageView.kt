package com.mjieg.composables.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 水波数据实体类
 */
data class Ripple(
    val cx: Float,          // 水波中心 X
    val cy: Float,          // 水波中心 Y
    val startTime: Long,     // 产生时间
    val duration: Long = 2000L // 存活时长（毫秒）
)

class RippleImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 网格密度（40x40 提供细腻的折射效果，同时保持流畅度）
    private val MESH_WIDTH = 40
    private val MESH_HEIGHT = 40
    private val COUNT = (MESH_WIDTH + 1) * (MESH_HEIGHT + 1)

    // 网格坐标数组 [x0, y0, x1, y1, ...]
    private val orig = FloatArray(COUNT * 2)
    private val verts = FloatArray(COUNT * 2)

    private var rawBitmap: Bitmap? = null
    private var scaledBitmap: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 水波队列（最大数量 5，使用线程安全集合）
    private val activeRipples = CopyOnWriteArrayList<Ripple>()
    private val MAX_RIPPLES = 5

    // 动画驱动开关
    private var isAnimating = false

    /**
     * 物理波形参数配置（可调整以改变水质感）
     */
    private val wavelength = 160f                       // 波长：数值越小，波纹越密
    private val k = (2 * Math.PI / wavelength).toFloat() // 波数
    private val waveSpeed = 650f                       // 扩散速度 (px/s)
    private val omega = waveSpeed * k                  // 角频率
    private val sigma = 70f                            // 高斯包络宽度（波纹带厚度）
    private val initialAmplitude = 40f                 // 初始水波振幅（扭曲强度）

    fun setImageResource(resId: Int) {
        rawBitmap = BitmapFactory.decodeResource(resources, resId)
        updateScaledBitmap()
        invalidate()
    }

    fun setImageBitmap(bitmap: Bitmap) {
        rawBitmap = bitmap
        updateScaledBitmap()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateScaledBitmap()
        initMesh()
    }

    private fun initMesh() {
        val w = width.toFloat()
        val h = height.toFloat()
        var index = 0
        for (y in 0..MESH_HEIGHT) {
            val fy = h * y / MESH_HEIGHT
            for (x in 0..MESH_WIDTH) {
                val fx = w * x / MESH_WIDTH
                orig[index * 2] = fx
                orig[index * 2 + 1] = fy
                verts[index * 2] = fx
                verts[index * 2 + 1] = fy
                index++
            }
        }
    }

    private fun updateScaledBitmap() {
        val bm = rawBitmap ?: return
        if (width > 0 && height > 0) {
            scaledBitmap = Bitmap.createScaledBitmap(bm, width, height, true)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            // 支持单点点击与多点触控（Multi-touch）
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                addRipple(event.getX(index), event.getY(index))
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 添加新的水波，维持队列最多 5 个
     */
    private fun addRipple(x: Float, y: Float) {
        if (activeRipples.size >= MAX_RIPPLES) {
            activeRipples.removeAt(0) // 超出 5 个时移除最旧的水波
        }
        activeRipples.add(Ripple(x, y, System.currentTimeMillis()))

        if (!isAnimating) {
            isAnimating = true
            postInvalidateOnAnimation()
        }
    }

    /**
     * 根据物理波形与多波叠加原理计算所有网格顶点的偏移
     */
    private fun updateMesh() {
        val currentTime = System.currentTimeMillis()

        // 移除已超时的水波
        activeRipples.removeAll { currentTime - it.startTime > it.duration }

        if (activeRipples.isEmpty()) {
            // 无水波时恢复原始网格
            System.arraycopy(orig, 0, verts, 0, orig.size)
            isAnimating = false
            return
        }

        var index = 0
        for (i in 0 until COUNT) {
            val ox = orig[i * 2]
            val oy = orig[i * 2 + 1]

            var totalDx = 0f
            var totalDy = 0f

            // 遍历所有活跃的水波并进行矢量叠加 (Wave Superposition)
            for (ripple in activeRipples) {
                val elapsedSec = (currentTime - ripple.startTime) / 1000f
                val progress = (elapsedSec * 1000f) / ripple.duration
                if (progress > 1.0f) continue

                val dx = ox - ripple.cx
                val dy = oy - ripple.cy
                val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                if (dist < 1e-3f) continue // 避免除以 0

                // 1. 波峰半径随时间扩散
                val currentRadius = waveSpeed * elapsedSec

                // 2. 距离差（与波峰相距多远）
                val deltaR = dist - currentRadius

                // 性能优化：超出高斯包络有效范围（>2.5*sigma）的网格点跳过计算
                if (abs(deltaR) > 2.5f * sigma) continue

                // 3. 高斯包络 (Gaussian Envelope)：让波形集中在扩散波峰附近
                val gaussian = exp(-(deltaR * deltaR) / (2 * sigma * sigma)).toFloat()

                // 4. 时间衰减：水波力量随时间减弱
                val timeDecay = (1f - progress) * (1f - progress)

                // 5. 正弦波列与振幅合成
                val amplitude = initialAmplitude * timeDecay * gaussian
                val phase = k * dist - omega * elapsedSec
                val offset = amplitude * sin(phase).toFloat()

                // 将径向位移累加到总偏移向量中
                totalDx += (dx / dist) * offset
                totalDy += (dy / dist) * offset
            }

            // 最终顶点坐标 = 原始坐标 + 叠加后的位移
            verts[index * 2] = ox + totalDx
            verts[index * 2 + 1] = oy + totalDy
            index++
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isAnimating) {
            updateMesh()
        }

        scaledBitmap?.let { bitmap ->
            canvas.drawBitmapMesh(
                bitmap,
                MESH_WIDTH,
                MESH_HEIGHT,
                verts,
                0,
                null,
                0,
                paint
            )
        }

        // 如果动画还在进行，驱动下一帧渲染 (保持 60 FPS / 120 FPS 高帧率)
        if (isAnimating) {
            postInvalidateOnAnimation()
        }
    }
}