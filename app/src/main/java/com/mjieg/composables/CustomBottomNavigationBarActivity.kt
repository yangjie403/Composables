package com.mjieg.composables

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mjieg.composables.views.CustomBottomNavigationBar

/** Simple XML-based test screen for [CustomBottomNavigationBar]. */
class CustomBottomNavigationBarActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_bottom_navigation_bar)

        val titleView = findViewById<TextView>(R.id.customBottomNavigationTitle)
        val eventView = findViewById<TextView>(R.id.customBottomNavigationEvent)
        val navigationBar = findViewById<CustomBottomNavigationBar>(R.id.customBottomNavigationBar)
        val tv1 = findViewById<TextView>(R.id.tv1)
        val tv2 = findViewById<TextView>(R.id.tv2)
        val container = findViewById<LinearLayout>(R.id.container)
        ViewCompat.setOnApplyWindowInsetsListener(container) { view, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.setPadding(0, top, 0, bottom)
            container.requestLayout()
            insets
        }
        navigationBar.setOnItemSelectedListener { index ->
            val title = if (index == 0) "Speed Test" else "Advanced"
            titleView.text = title
            eventView.text = "selectedIndex = $index"
        }

        navigationBar.setOnCenterClickListener {
            eventView.text = "center button clicked"
        }
        tv1.setOnClickListener {  }
        tv2.setOnClickListener {  }
    }
}
