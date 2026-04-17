package com.mjieg.composables

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import kotlin.math.hypot

class WhatsAppActivity : AppCompatActivity() {

    private lateinit var attachmentMenu: CardView
    private lateinit var btnAttach: ImageButton
    private var isMenuOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.whatsapp_layout)

        attachmentMenu = findViewById(R.id.attachmentMenu)
        btnAttach = findViewById(R.id.btnAttach)

        btnAttach.setOnClickListener {
            if (isMenuOpen) {
                hideMenu()
            } else {
                revealMenu()
            }
        }
    }

    private fun revealMenu() {
        // 1. 获取菜单在屏幕/父布局中的位置
        val menuLocation = IntArray(2)
        attachmentMenu.getLocationInWindow(menuLocation)

        // 2. 获取按钮在屏幕/父布局中的位置
        val buttonLocation = IntArray(2)
        btnAttach.getLocationInWindow(buttonLocation)

        // 3. 计算圆心相对于 attachmentMenu 本身的本地坐标
        // 计算公式：按钮中心点坐标 - 菜单起始点坐标
        val cx = (buttonLocation[0] + btnAttach.width / 2) - menuLocation[0]
        val cy = attachmentMenu.height // 动画从菜单的最底部边缘开始

        // 4. 计算最大半径：圆心到菜单最远顶点（通常是左上角）的距离
        val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

        // 确保在主线程执行动画
        val anim = ViewAnimationUtils.createCircularReveal(
            attachmentMenu,
            cx,
            cy,
            0f,
            finalRadius
        )

        anim.duration = 350
        attachmentMenu.visibility = View.VISIBLE
        anim.start()
        isMenuOpen = true
    }

    private fun hideMenu() {
        val menuLocation = IntArray(2)
        attachmentMenu.getLocationInWindow(menuLocation)
        val buttonLocation = IntArray(2)
        btnAttach.getLocationInWindow(buttonLocation)

        // 保持圆心计算逻辑一致
        val cx = (buttonLocation[0] + btnAttach.width / 2) - menuLocation[0]
        val cy = attachmentMenu.height

        val initialRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

        val anim = ViewAnimationUtils.createCircularReveal(
            attachmentMenu,
            cx,
            cy,
            initialRadius,
            0f
        )

        anim.duration = 350
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                attachmentMenu.visibility = View.INVISIBLE
            }
        })
        anim.start()
        isMenuOpen = false
    }

    // 处理返回键逻辑，如果菜单开着，按返回键先关闭菜单
    // override fun onBackPressed() {
    //     if (isMenuOpen) {
    //         hideMenu()
    //     } else {
    //         super.onBackPressed()
    //     }
    // }
}