package com.mjieg.composables.activities

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.AppBarLayout
import com.mjieg.composables.R
import com.mjieg.composables.views.CollapsibleToolbar

class MotionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_motion_coordinatorlayout)

        val icon = findViewById<ImageView>(R.id.icon)
        icon?.clipToOutline = true

        val appBar = findViewById<AppBarLayout>(R.id.app_bar)
        val toolbar = findViewById<CollapsibleToolbar>(R.id.constraintToolbar)

        ViewCompat.setOnApplyWindowInsetsListener(toolbar) {view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val minHeight = maxOf(dpToPx(56), topInset + dpToPx(32))
            view.minimumHeight = minHeight
            appBar.requestLayout()
            insets
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()
}