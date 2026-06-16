package com.mjieg.composables.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerAppScreen() {
    // 1. 定义抽屉状态，初始为关闭
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    // 2. 获取协程作用域，用于触发打开/关闭动画
    val scope = rememberCoroutineScope()

    // 记录当前选中的导航项
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("首页", "个人中心", "设置")

    // 3. 使用 ModalNavigationDrawer 构建抽屉
    ModalNavigationDrawer(
        drawerState = drawerState,
        // 是否允许手势滑动拉出/关闭抽屉（默认为 true）
        gesturesEnabled = true,
        // 4. 定义抽屉内部的内容
        drawerContent = {
            ModalDrawerSheet {
                // 抽屉顶部的 Header 区域
                Text(
                    text = "我的应用",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // 抽屉的导航列表
                items.forEachIndexed { index, item ->
                    NavigationDrawerItem(
                        label = { Text(text = item) },
                        selected = index == selectedItem,
                        onClick = {
                            selectedItem = index
                            // 点击后关闭抽屉
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        // 5. 这里是主屏幕的内容（抽屉关闭时显示的界面）
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("首页") },
                    navigationIcon = {
                        IconButton(onClick = {
                            // 点击菜单图标打开抽屉
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "打开菜单")
                        }
                    }
                )
            }
        ) { innerPadding ->
            // 主界面的具体内容
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = "这是主屏幕内容：${items[selectedItem]}",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}