package com.mjieg.composables.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.mjieg.composables.R
import kotlin.math.min

/**
 * 顶边正中间带半圆凹槽的 LinearLayout。
 *
 * 凹槽的圆心位于控件顶边的正中间：
 *
 *                 凹槽圆心
 *                    ●
 *       ─────────────   ─────────────
 *                     ╲     ╱
 *                      ╲___╱
 *
 * 实际绘制时，顶部边界会从左侧直线连接到半圆弧，再连接到右侧直线。
 * 半圆弧向 LinearLayout 内部延伸，因此顶部正中间会被裁剪掉一块半圆区域。
 *
 * XML 使用示例：
 *
 * <com.mjieg.composables.views.NotchedLinearLayout
 *     xmlns:app="http://schemas.android.com/apk/res-auto"
 *     android:layout_width="match_parent"
 *     android:layout_height="120dp"
 *     android:background="@android:color/white"
 *     app:notchRadius="48dp" />
 *
 * 注意：控件的背景和子 View 都会被同一条凹槽路径裁剪。如果需要让悬浮按钮
 * 覆盖在凹槽上方，可以把按钮作为同级 View 放在外层 FrameLayout 中，或者将
 * 按钮放在本布局中并保证它位于凹槽区域之外的绘制层级。
 */
class NotchedLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        // 未在 XML 中设置半径时使用的默认值。
        private const val DEFAULT_NOTCH_RADIUS_DP = 48f
    }

    private val density = resources.displayMetrics.density

    // 保存的是 px，因为 Canvas 和 Path 都使用像素坐标。
    private var notchRadiusPx: Float

    // 这条路径同时用于裁剪背景、子 View 和 foreground。
    private val clipPath = Path()

    init {
        val defaultRadius = DEFAULT_NOTCH_RADIUS_DP * density
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.NotchedLinearLayout,
            defStyleAttr,
            0,
        )
        notchRadiusPx = typedArray.getDimension(
            R.styleable.NotchedLinearLayout_notchRadius,
            defaultRadius,
        ).coerceAtLeast(0f)
        typedArray.recycle()

        // Android 7.x 及以下的硬件 Canvas 对非凸 clipPath 支持不完整。
        // 凹槽路径属于非凸路径，因此在这些版本上使用软件图层保证裁剪正确。
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    /**
     * 凹槽半径，单位为 dp。
     *
     * XML 中使用同名的 [notchRadius] 属性设置；代码中也可以直接写：
     * `layout.notchRadius = 48f`。
     *
     * 内部会自动把 dp 转换为 px，因为 Canvas 和 Path 使用的是像素坐标。
     */
    var notchRadius: Float
        get() = notchRadiusPx / density
        set(value) {
            require(value >= 0f) { "notchRadius must be greater than or equal to zero" }
            val radiusPx = value * density
            if (notchRadiusPx == radiusPx) return
            notchRadiusPx = radiusPx
            rebuildClipPath(width.toFloat(), height.toFloat())
            invalidate()
        }

    /** 使用 dp 设置凹槽半径。 */
    fun setNotchRadiusDp(radiusDp: Float) {
        notchRadius = radiusDp
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        // 尺寸变化时，凹槽的左右位置和可用半径都可能变化，需要重新生成路径。
        rebuildClipPath(width.toFloat(), height.toFloat())
    }

    override fun draw(canvas: Canvas) {
        if (width == 0 || height == 0) {
            super.draw(canvas)
            return
        }

        /*
         * 为什么在 draw() 中保留裁剪？
         *
         * View.draw() 的完整绘制流程大致是：
         *   背景 -> onDraw() -> dispatchDraw(子 View) -> foreground
         *
         * 这一层主要负责约束当前 LinearLayout 自身的背景、onDraw() 内容和 foreground。
         * 子 View 的 background、foreground 以及 ripple 则在下面的 dispatchDraw() 中
         * 再次裁剪。两层裁剪使用同一条 clipPath，分别覆盖父布局和子 View 的绘制流程。
         *
         * 不能只依赖 onDraw()，因为 onDraw() 执行时，子 View 还没有开始绘制；
         * 也不能只依赖 draw()，因为部分硬件加速或布局预览环境下，子 View 的绘制
         * 不一定会稳定继承外层的非凸裁剪区域。
         */
        val saveCount = canvas.save()
        canvas.clipPath(clipPath)
        super.draw(canvas)
        canvas.restoreToCount(saveCount)
    }

    override fun dispatchDraw(canvas: Canvas) {
        /*
         * LinearLayout 的子 View（例如 TextView）的 background、foreground 和 ripple
         * 都是在 dispatchDraw() 阶段绘制的。单独在 draw() 外层裁剪时，部分硬件加速
         * 或布局预览环境下，子 View 的绘制可能不会按预期继承这个非凸裁剪区域。
         *
         * 在这里再次使用同一条 clipPath，可以明确保证所有子 View 都只能绘制在
         * 半圆凹槽路径以内。draw() 中的裁剪仍然保留，用于约束父布局自身的背景和前景。
         */
        val saveCount = canvas.save()
        canvas.clipPath(clipPath)
        super.dispatchDraw(canvas)
        canvas.restoreToCount(saveCount)
    }

    /**
     * 根据当前控件尺寸生成裁剪路径。
     *
     * 路径的坐标以控件左上角为原点：x 向右，y 向下。
     * 顶边普通位置是 y = 0，凹槽会从 y = 0 向控件内部延伸到 y = radius。
     */
    private fun rebuildClipPath(viewWidth: Float, viewHeight: Float) {
        clipPath.reset()

        if (viewWidth <= 0f || viewHeight <= 0f) return

        // 半圆的直径不能超过控件宽度，否则左右端点会越界。
        val radius = min(notchRadiusPx, viewWidth / 2f)

        if (radius <= 0f) {
            // 半径为 0 时退化为普通矩形，避免 arcTo 使用零尺寸矩形。
            clipPath.addRect(0f, 0f, viewWidth, viewHeight, Path.Direction.CW)
            return
        }

        val centerX = viewWidth / 2f
        val leftNotchX = centerX - radius
        val rightNotchX = centerX + radius

        // 从左上角开始，沿顶部直线走到半圆的左端点。
        clipPath.moveTo(0f, 0f)
        clipPath.lineTo(leftNotchX, 0f)

        /*
         * 凹槽的核心：使用一个圆的下半圆连接左右两个端点。
         *
         * arcTo() 接收的是圆的外接矩形：
         *   left   = centerX - radius
         *   top    = -radius
         *   right  = centerX + radius
         *   bottom = radius
         *
         * 因为圆心是 (centerX, 0)，所以圆的上半部分位于控件外部，
         * 下半部分位于控件内部。路径从左端点开始：
         *   startAngle = 180°  -> 圆的最左侧
         *   sweepAngle = -180° -> 沿下半圆走到圆的最右侧
         *
         * 这样得到的边界会从 y=0 向下弯曲到 y=radius，形成真正的半圆凹槽。
         * 如果误用 +180°，路径会经过控件上方，视觉上就会变成向上的圆弧凸起。
        */
        clipPath.arcTo(
            leftNotchX,
            -radius,
            rightNotchX,
            radius,
            180f,
            -180f,
            false,
        )

        // 从半圆右端点连接到右上角，再沿底边回到左下角，闭合可见区域。
        clipPath.lineTo(viewWidth, 0f)
        clipPath.lineTo(viewWidth, viewHeight)
        clipPath.lineTo(0f, viewHeight)
        clipPath.close()
    }
}
