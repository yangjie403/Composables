package com.mjieg.composables

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import java.util.concurrent.CopyOnWriteArrayList

private const val TAG = "Application"
class App: Application() {

    val activityList = CopyOnWriteArrayList<Activity>()

    class AppLifecycleObserver(
        private val tracker: AppActivityTracker
    ) : DefaultLifecycleObserver {

        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)

            // 应用进入前台
            Log.d(TAG, "应用进入前台 (Foreground)")
            val top = tracker.topActivity

            // 如果应用回到前台，且当前栈顶 Activity 存在，且【不是】
            if (top != null && top !is WhatsAppActivity) {
                Log.d(TAG, "目前不是启动页: 进入启动页")
                val intent = Intent(top, WhatsAppActivity::class.java).apply {
                    // 视需求而定：可以加上无动画的 Flag，或者在 Activity 中处理
                    addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                }
                top.startActivity(intent)
            } else {
                Log.d(TAG, "目前已在启动页: 不需要进入启动页")
            }
        }
        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
            // 当应用进程首次启动时调用（仅触发一次）
            Log.d(TAG, "应用进程已创建")
        }

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            // 应用处于活跃状态
            Log.d(TAG, "应用处于活跃状态 (Resume)")
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            // 应用即将离开前台
            Log.d(TAG, "应用失去焦点 (Pause)")
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            // 应用进入后台
            Log.d(TAG, "应用进入后台 (Background)")
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            // 注意：由于系统是通过杀进程来终止应用的，此方法永远不会被触发。
        }
    }

    class AppActivityTracker : ActivityLifecycleCallbacks {

        // 维护一个当前存活的 Activity 列表
        private val aliveActivities = mutableListOf<Activity>()

        // 获取当前处于最顶部的 Activity
        val topActivity: Activity?
            get() = aliveActivities.lastOrNull()

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            aliveActivities.add(activity)
        }

        override fun onActivityStarted(activity: Activity) {
            // 将最新启动的 Activity 移到列表末尾，确保其代表栈顶
            aliveActivities.remove(activity)
            aliveActivities.add(activity)
        }

        override fun onActivityResumed(activity: Activity) {
            Log.d(TAG, "目前已启动的 Activity: \n" + aliveActivities.joinToString(separator = "\n") { it.javaClass.simpleName })
        }

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {
            aliveActivities.remove(activity)
            Log.d(TAG, "移除 Activity: ${activity.javaClass.simpleName} \n目前已启动的 Activity: \n" + aliveActivities.joinToString(separator = "\n") { it.javaClass.simpleName })
        }
    }

    val activityTracker = AppActivityTracker()

    override fun onCreate() {
        super.onCreate()
        // registerActivityLifecycleCallbacks(activityTracker)
        // ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver(activityTracker))
    }
}