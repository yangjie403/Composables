package com.mjieg.composables.screen

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private const val TAG = "Alphabet"

// 1. 模拟数据模型
data class Contact(val name: String, val initial: String)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AlphabetNavSampleScreen() {
    // 1. 模拟数据准备
    val contacts = remember {
        val names = listOf(
            "Alice", "Apple", "Bob", "Banner", "David", "Dog", "Edward",
            "Frank", "George", "Henry", "Ivan", "Jack", "Karl", "Linda", "Macy",
            "Nancy", "Oliver", "Peter", "Queen", "Robert", "Steve", "Tesla",
            "Ulysses", "Vivian", "William", "Xavier", "Yolanda", "Zebra"
        )
        names.map { Contact(it, it.take(1).uppercase()) }.sortedBy { it.name }
    }

    // 【核心修复 1】: 将联系人按照首字母分组
    val groupedContacts = remember(contacts) {
        contacts.groupBy { it.initial }
    }

    // val alphabet = remember { ('A'..'Z').map { it.toString() } + "#" }
    // 如果只需要显示联系人列表中存在的首字母
    val alphabet = remember(contacts) {
        contacts.map { it.initial }.distinct()
    }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 【核心修复 2】: 重新计算包含 StickyHeader 在内的绝对列表索引
    val indexMap = remember(groupedContacts) {
        val map = mutableMapOf<String, Int>()
        var currentIndex = 0

        // 1. 正向遍历分组，计算存在的字母所在的 LazyList Index
        groupedContacts.forEach { (initial, list) ->
            map[initial] = currentIndex
            // currentIndex 需要加上 Header(1个) 和 该组下的联系人数量
            currentIndex += 1 + list.size
        }

        // 2. 倒序遍历字母表，为不存在的字母填充“下一个最近字母”的 Index
        var nextAvailableIndex = currentIndex
        for (i in alphabet.indices.reversed()) {
            val letter = alphabet[i]
            if (map.containsKey(letter)) {
                nextAvailableIndex = map[letter]!!
            } else {
                map[letter] = nextAvailableIndex
            }
        }
        map
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("联系人") }) }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {

            // --- 底部：联系人列表 ---
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                // 【核心修复 3】: 直接遍历分组数据，避免手动判断 Index
                groupedContacts.forEach { (initial, contactsForInitial) ->
                    // 粘性标题占据一个 Item 位置
                    stickyHeader(key = initial) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF2F2F2))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(text = initial, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                    }

                    // 列表项占据后续的 Item 位置
                    items(contactsForInitial, key = { it.name }) { contact ->
                        ContactItem(contact.name)
                    }
                }
            }

            // --- 侧边：高性能字母导航条 ---
            AlphabetIndexer(
                alphabet = alphabet,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp),
                onLetterSelected = { letter ->
                    val targetIndex = indexMap[letter]
                    if (targetIndex != null) {
                        coroutineScope.launch {
                            // 由于索引已经精确指向 Header，这里不需要额外偏移，跳过去恰好顶格
                            listState.scrollToItem(targetIndex)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ContactItem(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // 增加背景色确保不会透出底部的文字，这也是防止滑动错觉的细节
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(0xFFE0E0E0)))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = name, fontSize = 16.sp, color = Color.Black)
    }
}

@Composable
fun AlphabetIndexer(
    alphabet: List<String>,
    modifier: Modifier = Modifier,
    onLetterSelected: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var activeIndex by remember { mutableIntStateOf(-1) }
    var barHeight by remember { mutableIntStateOf(0) }

    fun updateIndex(y: Float) {
        if (barHeight == 0) return
        val itemHeight = barHeight.toFloat() / alphabet.size
        // 计算索引并限制在合法范围内
        val index = (y / itemHeight).toInt().coerceIn(0, alphabet.lastIndex)
        if (index != activeIndex) {
            activeIndex = index
            onLetterSelected(alphabet[index])
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    Box(
        modifier = modifier
            .width(30.dp)
            .fillMaxHeight(0.8f) // 占据屏幕 80% 高度
            .onGloballyPositioned { barHeight = it.size.height }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        updateIndex(offset.y)
                        tryAwaitRelease()
                        activeIndex = -1
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> updateIndex(offset.y) },
                    onDrag = { change, _ -> updateIndex(change.position.y) },
                    onDragEnd = { activeIndex = -1 },
                    onDragCancel = { activeIndex = -1 }
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            // 【关键修复1】移除 verticalArrangement = Arrangement.SpaceEvenly
        ) {
            alphabet.forEachIndexed { index, letter ->
                val isSelected = activeIndex == index

                // 【关键修复2】使用 Box 加 weight(1f) 强行绝对等分高度
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // 每个 Box 占据精确的 1/N 高度
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray
                    )
                }
            }
        }

        if (activeIndex != -1) {
            val itemHeightPx = if (barHeight > 0) barHeight.toFloat() / alphabet.size else 0f

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        // 此时计算出的 Y 坐标和实际 UI 完全一致，气泡也会精准跟随手指
                        translationY =
                            (activeIndex * itemHeightPx) + (itemHeightPx / 2) - 30.dp.toPx()
                        translationX = -60.dp.toPx()
                    }
                    .size(60.dp)
                    .drawBehind {
                        drawCircle(color = Color(0xCC000000))
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = alphabet[activeIndex],
                    fontSize = 32.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}