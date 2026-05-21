package com.mjieg.composables.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicAlertDialogSample() {
    val openDialog = remember {
        mutableStateOf(false)
    }
    Button(
        onClick = {
            openDialog.value = true
        }
    ) {
        Text("Open dialog")
    }
    if (openDialog.value) {
        BasicAlertDialog(
            onDismissRequest = {
                openDialog.value = false
            }
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text =
                            "This area typically contains the supportive text " +
                                    "which presents the details regarding the Dialog's purpose."
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    TextButton(
                        onClick = { openDialog.value = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}


@Composable
fun AnimatedDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    // 1. 使用 MutableTransitionState 来控制和监听动画生命周期的各个阶段
    val transitionState = remember {
        MutableTransitionState(false)
    }

    // 2. 当外部传入的显示状态改变时，同步给动画的目标状态
    LaunchedEffect(show) {
        transitionState.targetState = show
    }

    // 5. 监听动画状态：当退出动画播放完毕（当前和目标状态均恢复为 false）时，回调通知父组件真正销毁 Dialog
    LaunchedEffect(transitionState.currentState, transitionState.targetState) {
        if (!transitionState.currentState && !transitionState.targetState) {
            onDismissRequest()
        }
    }

    // 3. 只要动画未播放完毕 (currentState为true) 或是 目标状态为显示 (targetState为true)，就保持 Dialog 挂载
    if (transitionState.currentState || transitionState.targetState) {
        Dialog(
            onDismissRequest = {
                // 拦截系统返回键或外部点击：不直接关闭，而是先触发退出动画
                transitionState.targetState = false
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false // 禁用平台默认宽度，以便我们自定义全屏遮罩和对话框大小
            )
        ) {
            // 全屏背景遮罩层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)) // 遮罩半透明颜色
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // 点击遮罩层触发退出动画
                        transitionState.targetState = false
                    },
                contentAlignment = Alignment.Center
            ) {
                // 4. 配置入场与出场动画效果
                AnimatedVisibility(
                    visibleState = transitionState,
                    enter = scaleIn(initialScale = 0.85f, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                    exit = scaleOut(targetScale = 0.85f, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                ) {
                    // 阻止点击对话框内部时，点击事件穿透到下层的遮罩层
                    Box(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {}
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
fun CustomDialogDemo() {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = { showDialog = true }) {
            Text("显示自定义弹窗")
        }

        // 使用自定义的动画弹窗
        AnimatedDialog(
            show = showDialog,
            onDismissRequest = { showDialog = false }
        ) {
            // 弹窗内部的具体卡片样式
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "确认操作",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "您确定要执行此操作吗？该操作在执行后将无法撤销。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            // 此时由于 showDialog 被设为 false，会触发 AnimatedDialog 内部的退出动画
                            showDialog = false
                        }) {
                            Text("取消")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            // 处理确认逻辑
                            showDialog = false
                        }) {
                            Text("确认")
                        }
                    }
                }
            }
        }
    }
}