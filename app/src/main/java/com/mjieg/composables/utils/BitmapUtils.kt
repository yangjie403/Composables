package com.mjieg.composables.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap

object BitmapUtils {
    
    /**
     * 将多个资源 ID 转换为 Bitmap 数组
     * 兼容 png/jpg 以及 xml 矢量图(VectorDrawable)
     */
    fun createBitmapArray(context: Context, vararg drawableIds: Int): Array<Bitmap> {
        val bitmapList = mutableListOf<Bitmap>()
        
        for (id in drawableIds) {
            val drawable = ContextCompat.getDrawable(context, id) ?: continue
            
            // 为了防止原始矢量图没大小或过大/过小，我们统一提取为固定的基础像素大小(例如 100x100)
            // 反正在 ParticleImageView 里最终都会被 dstRect 强制重新缩放，所以这里画大一点保证清晰度即可
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 100
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 100
            
            val bitmap = createBitmap(width, height)
            val canvas = Canvas(bitmap)
            
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            
            bitmapList.add(bitmap)
        }
        
        return bitmapList.toTypedArray()
    }
}