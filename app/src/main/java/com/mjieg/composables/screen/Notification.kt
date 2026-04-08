package com.mjieg.composables.screen

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mjieg.composables.MainActivity

@Composable
fun NotificationDemoScreen() {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    // 权限请求启动器
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasPermission = isGranted }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = if (hasPermission) "通知权限已获取" else "尚未获取通知权限")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (hasPermission) {
                // 4. 发送通知
                showSimpleNotification(context)
            } else {
                // 申请权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }) {
            Text(if (hasPermission) "发送通知" else "申请权限")
        }
    }
}

// 创建通知渠道 (Android 8.0+ 必须)
fun Context.createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Default Channel"
        val descriptionText = "用于演示通知的渠道"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("CHANNEL_ID_DEMO", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

// 发送通知的具体实现
private fun showSimpleNotification(context: Context) {
    val channelId = "CHANNEL_ID_DEMO"
    val notificationId = 101

    // A. 点击通知后要执行的意图：回到 MainActivity
    val intent = Intent(context, MainActivity::class.java)

    // B. 将 Intent 包装成 PendingIntent
    val pendingIntent: PendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE // Android 12+ 必须指定
    )

    // C. 构建通知内容
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info) // 设置图标
        .setContentTitle("通知标题")
        .setContentText("这是来自 Compose 应用的一条通知，点击我返回应用！")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent) // 设置点击行为
        .setAutoCancel(true) // 点击后自动消失

    // D. 发送
    with(NotificationManagerCompat.from(context)) {
        // 检查权限 (由于我们在 UI 层已检查，这里补充一下兼容性)
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            notify(notificationId, builder.build())
        }
    }
}

@Composable
fun NotificationPermissionFinalUltra() {
    val context = LocalContext.current
    val activity = context as Activity
    val sharedPrefs = remember { context.getSharedPreferences("permission_prefs", Context.MODE_PRIVATE) }

    var showSettingsDialog by remember { mutableStateOf(false) }

    // 动态检测通知是否真的开启（兼容所有 Android 版本）
    val isNotificationsEnabled = {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // 记录已经发起过请求
        sharedPrefs.edit().putBoolean("asked_notification_permission", true).apply()
        // 回调时不弹窗，等待下一次用户点击
    }

    LaunchedEffect(Unit) {
        val enabled = isNotificationsEnabled()

        if (!enabled) {
            // 情况 2: 通知被禁用，区分版本处理
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // --- Android 13+ 逻辑 ---
                val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, Manifest.permission.POST_NOTIFICATIONS
                )
                val hasAskedBefore = sharedPrefs.getBoolean("asked_notification_permission", false)

                when {
                    shouldShowRationale -> {
                        // 之前拒绝过，但没点“不再询问”，继续系统申请
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    !hasAskedBefore -> {
                        // 第一次申请
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    else -> {
                        // 已经永久拒绝过，或者用户手动在设置里关了，弹自定义窗
                        showSettingsDialog = true
                    }
                }
            } else {
                // --- Android 13 以下逻辑 ---
                // 没有动态权限概念，只要 disabled 了，一定是用户手动去设置关的
                // 直接弹窗引导用户去设置开启
                showSettingsDialog = true
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(if (isNotificationsEnabled()) "通知已开启" else "通知已禁用")

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            val enabled = isNotificationsEnabled()

            if (enabled) {
                // 情况 1: 通知已开启，执行发送逻辑
                showSimpleNotification(context)
            } else {
                // 情况 2: 通知被禁用，区分版本处理
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // --- Android 13+ 逻辑 ---
                    val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                        activity, Manifest.permission.POST_NOTIFICATIONS
                    )
                    val hasAskedBefore = sharedPrefs.getBoolean("asked_notification_permission", false)

                    when {
                        shouldShowRationale -> {
                            // 之前拒绝过，但没点“不再询问”，继续系统申请
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        !hasAskedBefore -> {
                            // 第一次申请
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        else -> {
                            // 已经永久拒绝过，或者用户手动在设置里关了，弹自定义窗
                            showSettingsDialog = true
                        }
                    }
                } else {
                    // --- Android 13 以下逻辑 ---
                    // 没有动态权限概念，只要 disabled 了，一定是用户手动去设置关的
                    // 直接弹窗引导用户去设置开启
                    showSettingsDialog = true
                }
            }
        }) {
            Text("检查权限并发送通知")
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("通知权限受限") },
            text = { Text("检测到您的通知开关已关闭。为了能及时收到消息，请前往设置开启通知。") },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    openAppNotificationSettings(context)
                }) {
                    Text("去设置")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 跳转到应用的通知设置页面
 */
fun openAppNotificationSettings(context: Context) {
    val intent = Intent().apply {
        when {
            // Android 8.0 及以上，直接跳转到“通知设置”子页面
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            // Android 5.0 - 7.1
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                action = "android.settings.APP_NOTIFICATION_SETTINGS"
                putExtra("app_package", context.packageName)
                putExtra("app_uid", context.applicationInfo.uid)
            }
            // 低版本跳转到应用详情页
            else -> {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}