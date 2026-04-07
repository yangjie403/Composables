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
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

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
fun NotificationPermissionFinalSolution() {
    val context = LocalContext.current
    val activity = context as Activity

    // 使用 SharedPreferences 记录是否曾经申请过权限
    val sharedPrefs = remember { context.getSharedPreferences("permission_prefs", Context.MODE_PRIVATE) }

    // 状态管理
    var showSettingsDialog by remember { mutableStateOf(false) }
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    // 权限申请启动器
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        // 只要触发了系统申请，就标记为“已申请过”
        sharedPrefs.edit().putBoolean("asked_notification_permission", true).apply()

        // 注意：这里我们不在回调里弹出自定义对话框，保证了拒绝后的“沉默”
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(if (hasPermission) "权限已获得" else "权限未获得")

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val checkResult = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                val isGranted = checkResult == PackageManager.PERMISSION_GRANTED
                val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
                val hasAskedBefore = sharedPrefs.getBoolean("asked_notification_permission", false)

                when {
                    isGranted -> {
                        // 已有权限，执行发送通知逻辑
                        showSimpleNotification(context)
                    }
                    shouldShowRationale -> {
                        // 用户之前拒绝过，但没勾选“不再询问”，继续尝试系统申请
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    !hasAskedBefore -> {
                        // 第一次申请，直接调用系统弹窗
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    else -> {
                        // 重点：走到这里说明 isGranted=false, shouldShowRationale=false, 且 hasAskedBefore=true
                        // 这意味着用户之前勾选了“不再询问”。
                        // 此时我们才显示自定义的引导对话框。
                        showSettingsDialog = true
                    }
                }
            }
        }) {
            Text("申请权限 / 发送通知")
        }
    }

    // 自定义引导对话框
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("通知功能已禁用") },
            text = { Text("您之前拒绝了通知权限并选择了“不再询问”。为了正常使用，请前往设置手动开启。") },
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