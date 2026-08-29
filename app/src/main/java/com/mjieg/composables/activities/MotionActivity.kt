package com.mjieg.composables.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.AppBarLayout
import com.mjieg.composables.R
import com.mjieg.composables.views.CollapsibleToolbar

class MotionActivity : ComponentActivity() {

    private lateinit var container: View

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // setContentView(R.layout.activity_motion_coordinatorlayout)
        // setContentView(R.layout.motion_1_coordination)
        // setContentView(R.layout.motion_2_coordination)
        setContentView(R.layout.motion_3_reveal)

        container = findViewById(R.id.motionLayout)
        val icon = findViewById<ImageView>(R.id.icon)
        icon?.clipToOutline = true

        val appBar = findViewById<AppBarLayout>(R.id.app_bar)
        val toolbar = findViewById<CollapsibleToolbar>(R.id.constraintToolbar)

        if (appBar != null && toolbar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
                val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                val minHeight = maxOf(dpToPx(56), topInset + dpToPx(32))
                view.minimumHeight = minHeight
                appBar.requestLayout()
                insets
            }
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    fun changeState(v: View?) {
        val motionLayout = container as? MotionLayout ?: return
        if (motionLayout.progress > 0.5f) {
            motionLayout.transitionToStart()
        } else {
            motionLayout.transitionToEnd()
        }
    }
}