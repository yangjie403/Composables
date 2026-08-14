package com.mjieg.composables

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class ParticleImageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_particle_image)


        // val particleImageView = findViewById<ParticleImageView>(R.id.particleImageView)
        // val stickers = BitmapUtils.createBitmapArray(
        //     this,
        //     R.drawable.bg_bn_1,
        //     R.drawable.bg_bn_2,
        //     R.drawable.bg_bn_3,
        //     R.drawable.bg_bn_4,
        //     R.drawable.bg_bn_5,
        //     R.drawable.bg_bn_6,
        //     R.drawable.bg_bn_7,
        //     R.drawable.bg_bn_8,
        //     R.drawable.bg_bn_9,
        //     R.drawable.bg_bn_10,
        // )
        // particleImageView.setParticleBitmaps(stickers)
    }
}