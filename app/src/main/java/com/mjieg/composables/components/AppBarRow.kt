package com.mjieg.composables.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SimpleTopAppBarWithAdaptiveActions() {
    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
    // Material guidelines state 3 items max in compact, and 5 items max elsewhere.
    // To test this, try a resizable emulator, or a phone in landscape and portrait orientation.
    val maxItemCount =
        if (sizeClass.minWidthDp >= WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) {
            5
        } else {
            3
        }
    val icons =
        listOf(
            Icons.Filled.Attachment,
            Icons.Filled.Edit,
            Icons.Outlined.Star,
            Icons.Filled.Snooze,
            Icons.Outlined.MarkEmailUnread,
        )
    val items = listOf("Attachment", "Edit", "Star", "Snooze", "Mark unread")
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Simple TopAppBar", maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    TooltipBox(
                        positionProvider =
                            TooltipDefaults.rememberTooltipPositionProvider(
                                TooltipAnchorPosition.Above
                            ),
                        tooltip = { PlainTooltip { Text("Menu") } },
                        state = rememberTooltipState(),
                    ) {
                        IconButton(onClick = { /* doSomething() */ }) {
                            Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                },
                actions = {
                    AppBarRow(
                        maxItemCount = maxItemCount,
                        overflowIndicator = {
                            TooltipBox(
                                positionProvider =
                                    TooltipDefaults.rememberTooltipPositionProvider(
                                        TooltipAnchorPosition.Above
                                    ),
                                tooltip = { PlainTooltip { Text("Overflow") } },
                                state = rememberTooltipState(),
                            ) {
                                IconButton(onClick = { it.show() }) {
                                    Icon(
                                        imageVector = Icons.Filled.MoreVert,
                                        contentDescription = "Overflow",
                                    )
                                }
                            }
                        },
                    ) {
                        items.forEachIndexed { index, item ->
                            clickableItem(
                                onClick = {},
                                icon = {
                                    Icon(imageVector = icons[index], contentDescription = item)
                                },
                                label = item,
                            )
                        }
                    }
                },
            )
        },
        content = { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val list = (0..75).map { it.toString() }
                items(count = list.size) {
                    Text(
                        text = list[it],
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TopAppBarWithToggleableAndCustomItems() {
    var isFavorite by remember { mutableStateOf(false) }
    var isNotificationEnabled by remember { mutableStateOf(false) }
    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass

    var switchChecked by remember { mutableStateOf(false) }
    var checkboxChecked by remember { mutableStateOf(false) }
    val maxItemCount =
        if (sizeClass.minWidthDp >= WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) {
            5
        } else {
            3
        }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Toggleable & Custom Items",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    AppBarRow(
                        maxItemCount = maxItemCount,
                        overflowIndicator = {
                            IconButton(onClick = { it.show() }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "More options"
                                )
                            }
                        }
                    ) {
                        toggleableItem(
                            checked = isFavorite,
                            onCheckedChange = { isFavorite = it },
                            icon = {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = "Favorite"
                                )
                            },
                            label = "Favorite"
                        )

                        toggleableItem(
                            checked = isNotificationEnabled,
                            onCheckedChange = { isNotificationEnabled = it },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Snooze,
                                    contentDescription = "Notifications",
                                    tint = if (isNotificationEnabled) Color.Red else Color.Gray
                                )
                            },
                            label = "Notifications"
                        )

                        customItem(
                            appbarContent = {
                                Switch(
                                    checked = switchChecked,
                                    onCheckedChange = { switchChecked = it },
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            },
                            menuContent = { menuState ->
                                DropdownMenuItem(
                                    text = { Text("Switch") },
                                    trailingIcon = {
                                        Switch(
                                            checked = switchChecked,
                                            onCheckedChange = {
                                                switchChecked = it
                                                menuState.dismiss()
                                            }
                                        )
                                    },
                                    onClick = { }
                                )
                            }
                        )

                        customItem(
                            appbarContent = {
                                Checkbox(
                                    checked = checkboxChecked,
                                    onCheckedChange = { checkboxChecked = it },
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            },
                            menuContent = { menuState ->
                                DropdownMenuItem(
                                    text = { Text("Checkbox") },
                                    trailingIcon = {
                                        Checkbox(
                                            checked = checkboxChecked,
                                            onCheckedChange = {
                                                checkboxChecked = it
                                                menuState.dismiss()
                                            }
                                        )
                                    },
                                    onClick = { }
                                )
                            }
                        )

                        clickableItem(
                            onClick = { },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Attachment,
                                    contentDescription = "Attachment"
                                )
                            },
                            label = "Attach"
                        )

                        clickableItem(
                            onClick = { },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit"
                                )
                            },
                            label = "Edit"
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "Favorite: ${if (isFavorite) "Enabled" else "Disabled"}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                item {
                    Text(
                        text = "Notifications: ${if (isNotificationEnabled) "Enabled" else "Disabled"}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                val list = (0..50).map { it.toString() }
                items(count = list.size) {
                    Text(
                        text = "Item ${list[it]}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }
    )
}
