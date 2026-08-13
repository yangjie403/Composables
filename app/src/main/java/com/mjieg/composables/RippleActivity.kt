package com.mjieg.composables

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mjieg.composables.views.RippleImageView

class RippleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ripple)

        val rippleImageView = findViewById<RippleImageView>(R.id.rippleImageView)

        // 设置你的图片资源 (请在 res/drawable 中放入一张名为 sample_image 的图片)
        rippleImageView.setImageResource(R.drawable.wallpaper)
    }
}