package com.mjieg.composables.views

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.graphics.PathParser
import androidx.core.graphics.toColorInt
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Compose 版
 * [com.mjieg.composables.components.CustomBottomNavigationBar] 的传统 View 实现。
 *
 * 这个 View 由三部分组成：
 * 1. 底部的白色导航栏；
 * 2. 左右两个可选中的导航项；
 * 3. 位于顶部中央、覆盖在凹槽上的悬浮按钮。
 *
 * 所有内容都通过 Canvas 绘制，因此不依赖 Compose，也不需要额外的 XML
 * drawable 资源。这个 View 可以直接在 XML 中声明，也可以在代码中创建，
 * 然后通过 [setOnItemSelectedListener] 和 [setOnCenterClickListener] 监听事件。
 */
class CustomBottomNavigationBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    companion object {
        // 两个导航项的索引，与 Compose 版本保持一致：0 表示 Speed Test，1 表示 Advanced。
        private const val ITEM_SPEED_TEST = 0
        private const val ITEM_ADVANCED = 1

        // 触摸事件的内部目标标记。
        private const val TARGET_NONE = -1
        private const val TARGET_CENTER = 2

        // 设计尺寸，单位为 dp。
        // 整个 View 高 120dp，其中底部导航栏高 80dp，中央按钮有 40dp 位于导航栏上方。
        private const val BAR_HEIGHT_DP = 80f
        private const val CONTAINER_HEIGHT_DP = 120f
        private const val CENTER_BUTTON_SIZE_DP = 80f

        // 凹槽半径为 48dp，比中央按钮半径 40dp 多 8dp，
        // 用于让中央按钮和凹槽之间留出与 Compose 版一致的间距。
        private const val NOTCH_RADIUS_DP = 48f
        private const val ITEM_ICON_SIZE_DP = 30f
        private const val CENTER_ICON_SIZE_DP = 42f
        private const val ITEM_TEXT_SIZE_SP = 16f

        // 当父布局没有给出明确宽度时，wrap_content 使用的默认宽度。
        private const val DEFAULT_WIDTH_DP = 360f

        // 颜色与 Compose 版本中的颜色保持一致。
        private const val COLOR_WHITE = Color.WHITE
        private val COLOR_SELECTED = "#0878CE".toColorInt()
        private val COLOR_UNSELECTED = "#707070".toColorInt()
        private val COLOR_CENTER_ICON = "#777777".toColorInt()
        private val COLOR_SHADOW = 0x55000000
        private val COLOR_PRESS = 0x18000000

        // Material Icon 的 path 数据直接保存在这里。
        // 这样 View 版不需要引用 Compose 的 ImageVector，也不需要单独创建 drawable XML。
        private const val SPEED_PATH =
            "M20.38 8.63A9.03 9.03 0 0 0 12 3a9.03 9.03 0 0 0-8.38 5.63l1.84.77A7.02 7.02 0 0 1 12 5c2.89 0 5.37 1.75 6.54 4.23l1.84-.6zM4 18h16v2H4v-2zm8-9a1 1 0 0 0-1 1v3.59L8.29 16.3l1.42 1.42L12.41 15H14v-2h-1v-3a1 1 0 0 0-1-1z"
        private const val SETTINGS_PATH =
            "M19.43 12.98c.04-.32.07-.65.07-.98s-.02-.66-.07-.98l2.11-1.65c.19-.15.24-.42.12-.64l-2-3.46c-.12-.22-.37-.31-.6-.22l-2.49 1c-.52-.4-1.08-.73-1.69-.98L14.5 2.42C14.47 2.18 14.25 2 14 2h-4c-.25 0-.46.18-.5.42L9.12 5.07c-.61.25-1.17.59-1.69.98l-2.49-1c-.23-.08-.48 0-.6.22l-2 3.46c-.13.22-.07.49.12.64l2.11 1.65c-.04.32-.08.65-.08.98s.03.66.08.98l-2.11 1.65c-.19.15-.24.42-.12.64l2 3.46c.12.22.37.31.6.22l2.49-1c.52.4 1.08.73 1.69.98l.38 2.65c.04.24.25.42.5.42h4c.25 0 .46-.18.5-.42l.38-2.65c.61-.25 1.17-.58 1.69-.98l2.49 1c.23.08.48 0 .6-.22l2-3.46c.12-.22.07-.49-.12-.64l-2.11-1.65zM12 15.5A3.5 3.5 0 1 1 12 8a3.5 3.5 0 0 1 0 7.5z"
        private const val SWAP_HORIZ_PATH =
            "M6.99 11 3 15l3.99 4v-3H14v-2H6.99v-3zM21 9l-3.99-4v3H10v2h7.01v3L21 9z"

        private fun parsePath(pathData: String): Path =
            requireNotNull(PathParser.createPathFromPathData(pathData))
    }

    /** 导航项选中后的回调，index 只可能是 0 或 1。 */
    fun interface OnItemSelectedListener {
        fun onItemSelected(index: Int)
    }

    /** 中央悬浮按钮点击后的回调。 */
    fun interface OnCenterClickListener {
        fun onCenterClick()
    }

    // density 用于把 dp 转换为 px；fontScale 用于让文字的 sp 大小跟随系统字体缩放。
    private val density = resources.displayMetrics.density
    private val scaledDensity = density * resources.configuration.fontScale

    // Canvas 只能使用像素绘制，所以初始化时就把设计尺寸转换成像素保存下来。
    private val barHeight = dp(BAR_HEIGHT_DP)
    private val containerHeight = dp(CONTAINER_HEIGHT_DP)
    private val centerButtonRadius = dp(CENTER_BUTTON_SIZE_DP / 2f)
    private val notchRadius = dp(NOTCH_RADIUS_DP)
    private val itemIconSize = dp(ITEM_ICON_SIZE_DP)
    private val centerIconSize = dp(CENTER_ICON_SIZE_DP)

    // selectedIndexInternal 是真正保存状态的字段，外部通过 selectedIndex 属性访问。
    private var selectedIndexInternal = ITEM_SPEED_TEST

    // 当前正在按下的区域：两个导航项分别使用 0、1，中央按钮使用 TARGET_CENTER。
    private var pressedTarget = TARGET_NONE

    // ACTION_UP 时先记录要执行的目标，再通过 performClick() 统一触发回调。
    // 这样可以遵循 Android 对辅助功能点击事件的处理约定。
    private var pendingClickTarget = TARGET_NONE

    // 两个导航项当前实际绘制的颜色。选中状态变化时，这两个值会被动画逐帧更新。
    private val itemColors = intArrayOf(COLOR_SELECTED, COLOR_UNSELECTED)
    private val colorAnimators = arrayOfNulls<ValueAnimator>(2)

    private var itemSelectedListener: OnItemSelectedListener? = null
    private var centerClickListener: OnCenterClickListener? = null

    // 导航栏画笔。阴影会跟随 navigationBarPath 的轮廓绘制，包含凹槽边缘的阴影。
    private val navigationBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = COLOR_WHITE
        setShadowLayer(dp(3f), 0f, dp(1f), COLOR_SHADOW)
    }

    // 中央悬浮按钮画笔。中央按钮单独绘制在导航栏之后，确保它覆盖在凹槽之上。
    private val centerButtonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = COLOR_WHITE
        setShadowLayer(dp(8f), 0f, dp(3f), COLOR_SHADOW)
    }
    // 按压反馈使用半透明黑色覆盖层，实际绘制时会被 navigationBarPath 裁剪。
    private val pressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = COLOR_PRESS
    }

    // 图标使用填充 path 绘制；文字使用系统的 sans-serif-medium 字体模拟 Compose 的 Medium 字重。
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = sp(ITEM_TEXT_SIZE_SP)
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
    }

    // 导航栏的真实轮廓路径。
    // 这个 Path 不仅用于绘制白色导航栏，也用于裁剪导航项的按压反馈，
    // 从而保证反馈不会越过中央凹槽的边界。
    private val navigationBarPath = Path()

    // 图标绘制时，使用 transformedIconPath 保存缩放、平移后的临时路径，避免修改原始 path。
    private val transformedIconPath = Path()
    private val iconMatrix = Matrix()
    private val speedPath = parsePath(SPEED_PATH)
    private val settingsPath = parsePath(SETTINGS_PATH)
    private val swapHorizPath = parsePath(SWAP_HORIZ_PATH)

    /** 当前选中的导航项：0 表示 Speed Test，1 表示 Advanced。 */
    var selectedIndex: Int
        get() = selectedIndexInternal
        set(value) {
            require(value == ITEM_SPEED_TEST || value == ITEM_ADVANCED) {
                "selectedIndex must be 0 (Speed Test) or 1 (Advanced)"
            }
            if (selectedIndexInternal == value) return
            selectedIndexInternal = value
            updateItemColors(animated = isLaidOut)
            // 状态变化后通知辅助功能服务，例如 TalkBack。
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED)
        }

    init {
        // 自定义 View 自己处理点击，所以必须声明为可点击、可聚焦。
        isClickable = true
        isFocusable = true
        contentDescription = "Speed Test, Advanced, Switch mode"

        // Paint.setShadowLayer 对自绘路径的阴影支持依赖软件图层。
        // 这个控件尺寸较小，使用软件图层可以让 API 24+ 的阴影效果更稳定。
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun setOnItemSelectedListener(listener: OnItemSelectedListener?) {
        itemSelectedListener = listener
    }

    fun setOnCenterClickListener(listener: OnCenterClickListener?) {
        centerClickListener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 宽度优先服从父布局约束；没有明确宽度时使用 360dp。
        // 高度至少需要 120dp，否则中央按钮或凹槽会被裁剪。
        val desiredWidth = dp(DEFAULT_WIDTH_DP).toInt()
        val desiredHeight = max(containerHeight.toInt(), suggestedMinimumHeight)
        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec),
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val barTop = height - barHeight
        val safeNotchRadius = min(notchRadius, max(1f, centerX - 1f))
        val centerY = barTop

        // 绘制顺序很重要：先绘制导航栏，再绘制导航项，最后绘制中央悬浮按钮。
        // 这样中央按钮可以覆盖在凹槽上方，同时导航项仍然位于白色导航栏内部。
        drawNavigationBar(canvas, centerX, barTop, safeNotchRadius)
        drawNavigationItems(canvas, centerX, barTop)
        drawCenterButton(canvas, centerX, centerY)
    }

    private fun drawNavigationBar(canvas: Canvas, centerX: Float, barTop: Float, radius: Float) {
        /*
         * 凹槽的实现原理：
         *
         * 1. 导航栏的普通顶部边界位于 barTop，也就是 View 底部向上 80dp 的位置。
         * 2. 先从左上角移动到凹槽左侧的切点 (centerX - radius, barTop)。
         * 3. 以 (centerX, barTop) 为圆心、radius 为半径构造一个圆的边界框。
         *    这个圆的顶部会超出导航栏，底部会进入导航栏内部。
         * 4. arcTo 从圆的最左侧开始，沿屏幕坐标系中的下半圆走 180 度，
         *    最终到达圆的最右侧。于是顶部边界会从 barTop 向下弯入导航栏，形成一个凹口。
         * 5. 再沿右侧顶部、右下角、左下角回到起点，闭合路径并填充白色。
         *
         * 中央按钮的半径是 40dp，而凹槽半径是 48dp，因此凹槽会比按钮轮廓大 8dp，
         * 形成按钮周围的留白与阴影空间。这里不单独绘制一个“圆形缺口”，
         * 而是直接改变导航栏顶部边界，所以阴影也能自然跟随凹槽曲线。
         */
        navigationBarPath.reset()

        // 左侧顶部直线，直到凹槽的左侧切点。
        navigationBarPath.moveTo(0f, barTop)
        navigationBarPath.lineTo(centerX - radius, barTop)

        // 圆的外接矩形：圆心位于 (centerX, barTop)，直径为 radius * 2。
        navigationBarPath.arcTo(
            centerX - radius,
            barTop - radius,
            centerX + radius,
            barTop + radius,
            180f,
            -180f,
            false,
        )

        // 从凹槽右侧切点继续连接到右上角，再沿底边闭合整个导航栏区域。
        navigationBarPath.lineTo(width.toFloat(), barTop)
        navigationBarPath.lineTo(width.toFloat(), height.toFloat())
        navigationBarPath.lineTo(0f, height.toFloat())
        navigationBarPath.close()

        // 使用同一条路径绘制背景和阴影，保证阴影也包含凹槽的曲线轮廓。
        canvas.drawPath(navigationBarPath, navigationBarPaint)
    }

    private fun drawNavigationItems(canvas: Canvas, centerX: Float, barTop: Float) {
        // 导航项的按压反馈本质上是左右两个矩形覆盖层。
        // 如果直接绘制矩形，它们会覆盖中央凹槽区域；因此这里先把 Canvas
        // 裁剪为导航栏的真实路径，后续的图标、文字和按压反馈都只能出现在该路径内。
        canvas.save()
        canvas.clipPath(navigationBarPath)

        val fontMetrics = textPaint.fontMetrics
        val textHeight = fontMetrics.bottom - fontMetrics.top
        val contentHeight = itemIconSize + textHeight
        val contentTop = barTop + (barHeight - contentHeight) / 2f
        val iconCenterY = contentTop + itemIconSize / 2f
        val textBaseline = contentTop + itemIconSize - fontMetrics.top

        drawItem(
            canvas = canvas,
            centerX = centerX / 2f,
            iconCenterY = iconCenterY,
            textBaseline = textBaseline,
            iconPath = speedPath,
            title = "Speed Test",
            color = itemColors[ITEM_SPEED_TEST],
            pressed = pressedTarget == ITEM_SPEED_TEST,
        )
        drawItem(
            canvas = canvas,
            centerX = centerX + centerX / 2f,
            iconCenterY = iconCenterY,
            textBaseline = textBaseline,
            iconPath = settingsPath,
            title = "Advanced",
            color = itemColors[ITEM_ADVANCED],
            pressed = pressedTarget == ITEM_ADVANCED,
        )

        canvas.restore()
    }

    private fun drawItem(
        canvas: Canvas,
        centerX: Float,
        iconCenterY: Float,
        textBaseline: Float,
        iconPath: Path,
        title: String,
        color: Int,
        pressed: Boolean,
    ) {
        if (pressed) {
            // 矩形只负责描述对应导航项的左右区域，真正限制其边界的是上层的 clipPath。
            val itemLeft = if (centerX < width / 2f) 0f else width / 2f
            val itemRight = if (centerX < width / 2f) width / 2f else width.toFloat()
            canvas.drawRect(itemLeft, height - barHeight, itemRight, height.toFloat(), pressPaint)
        }
        drawIcon(canvas, iconPath, centerX, iconCenterY, itemIconSize, color)
        textPaint.color = color
        canvas.drawText(title, centerX, textBaseline, textPaint)
    }

    private fun drawCenterButton(canvas: Canvas, centerX: Float, centerY: Float) {
        // 中央按钮最后绘制，使白色圆形覆盖凹槽内部，并保留独立的悬浮阴影。
        canvas.drawCircle(centerX, centerY, centerButtonRadius, centerButtonPaint)
        if (pressedTarget == TARGET_CENTER) {
            // 中央按钮是圆形反馈，不需要使用导航栏的 clipPath。
            canvas.drawCircle(centerX, centerY, centerButtonRadius, pressPaint)
        }
        drawIcon(canvas, swapHorizPath, centerX, centerY, centerIconSize, COLOR_CENTER_ICON)
    }

    private fun drawIcon(
        canvas: Canvas,
        sourcePath: Path,
        centerX: Float,
        centerY: Float,
        size: Float,
        color: Int,
    ) {
        // Material Icon 的原始坐标系是 24 x 24，这里统一缩放到目标尺寸，
        // 再把图标平移到指定的中心点。
        iconMatrix.reset()
        iconMatrix.setScale(size / 24f, size / 24f)
        iconMatrix.postTranslate(centerX - size / 2f, centerY - size / 2f)
        sourcePath.transform(iconMatrix, transformedIconPath)
        iconPaint.color = color
        canvas.drawPath(transformedIconPath, iconPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // 优先判断中央圆形按钮，因为它和左右导航项在垂直方向上存在重叠。
                pressedTarget = targetAt(event.x, event.y)
                if (pressedTarget == TARGET_NONE) return false
                parent?.requestDisallowInterceptTouchEvent(true)
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                // 手指滑出原来的目标区域后取消按压反馈，避免抬手时误触发。
                val target = targetAt(event.x, event.y)
                if (target != pressedTarget) {
                    pressedTarget = TARGET_NONE
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                // 只有按下和抬起仍然落在同一个区域时，才认为是一次有效点击。
                val target = pressedTarget
                pressedTarget = TARGET_NONE
                invalidate()
                if (target != TARGET_NONE && target == targetAt(event.x, event.y)) {
                    pendingClickTarget = target
                    performClick()
                }
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                // 父布局抢走事件或系统取消手势时，清除按压状态。
                pressedTarget = TARGET_NONE
                invalidate()
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        // 调用父类实现，保持 View 点击事件的标准行为，再分发自定义区域回调。
        super.performClick()
        when (pendingClickTarget) {
            ITEM_SPEED_TEST,
            ITEM_ADVANCED,
            -> {
                val item = pendingClickTarget
                selectedIndex = item
                itemSelectedListener?.onItemSelected(item)
            }

            TARGET_CENTER -> centerClickListener?.onCenterClick()
        }
        pendingClickTarget = TARGET_NONE
        return true
    }

    private fun targetAt(x: Float, y: Float): Int {
        val centerX = width / 2f
        val barTop = height - barHeight
        val distanceFromCenter = hypot(x - centerX, y - barTop)
        // 中央按钮的圆心正好位于导航栏顶部边界的中点。
        // 必须使用圆形命中区域，而不是简单的 80dp 方形区域，才能和视觉轮廓一致。
        if (distanceFromCenter <= centerButtonRadius) return TARGET_CENTER
        // 中央按钮之外，只有导航栏底部 80dp 区域可以响应左右导航项点击。
        if (y >= barTop) return if (x < centerX) ITEM_SPEED_TEST else ITEM_ADVANCED
        return TARGET_NONE
    }

    private fun updateItemColors(animated: Boolean) {
        for (item in 0..1) {
            val targetColor = if (item == selectedIndexInternal) COLOR_SELECTED else COLOR_UNSELECTED
            colorAnimators[item]?.cancel()
            if (!animated) {
                // 初次创建或恢复状态时直接设置颜色，不播放动画。
                itemColors[item] = targetColor
                continue
            }

            // 使用 ArgbEvaluator 对颜色的 ARGB 分量做插值，模拟 Compose 中的颜色过渡。
            colorAnimators[item] = ValueAnimator.ofObject(
                ArgbEvaluator(),
                itemColors[item],
                targetColor,
            ).apply {
                duration = 220L
                addUpdateListener { animator ->
                    itemColors[item] = animator.animatedValue as Int
                    invalidate()
                }
                start()
            }
        }
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable {
        // 保存选中项，避免屏幕旋转或 Activity 重建后回到默认的 Speed Test。
        return SavedState(super.onSaveInstanceState(), selectedIndexInternal)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            selectedIndexInternal = state.selectedIndex
            // 恢复状态时直接刷新颜色，避免重建期间出现错误的选中颜色。
            updateItemColors(animated = false)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private fun dp(value: Float): Float = value * density

    private fun sp(value: Float): Float = value * scaledDensity

    private class SavedState : BaseSavedState {
        val selectedIndex: Int

        constructor(superState: Parcelable?, selectedIndex: Int) : super(superState) {
            this.selectedIndex = selectedIndex
        }

        private constructor(source: Parcel) : super(source) {
            selectedIndex = source.readInt()
        }

        override fun writeToParcel(destination: Parcel, flags: Int) {
            super.writeToParcel(destination, flags)
            destination.writeInt(selectedIndex)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source)

                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }
}
