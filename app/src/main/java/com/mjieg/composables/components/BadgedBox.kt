package com.mjieg.composables.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.mjieg.composables.ui.PreviewLayout

@Preview
@Composable
fun NavigationBarItemWithBadge() {
    PreviewLayout {
        NavigationBar {
            NavigationBarItem(
                icon = {
                    BadgedBox(
                        badge = {
                            Badge(
                                modifier =
                                    Modifier.semantics { contentDescription = "New notification" }
                            )
                        }
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = "Favorite")
                    }
                },
                selected = false,
                onClick = {},
            )
            NavigationBarItem(
                icon = {
                    BadgedBox(
                        badge = {
                            Badge {
                                val badgeNumber = "8"
                                Text(
                                    badgeNumber,
                                    modifier =
                                        Modifier.semantics {
                                            contentDescription = "$badgeNumber new notifications"
                                        },
                                )
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = "Favorite")
                    }
                },
                selected = false,
                onClick = {},
            )
            NavigationBarItem(
                icon = {
                    BadgedBox(
                        badge = {
                            Badge {
                                val badgeNumber = "999+"
                                Text(
                                    badgeNumber,
                                    modifier =
                                        Modifier.semantics {
                                            contentDescription = "$badgeNumber new notifications"
                                        },
                                )
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = "Favorite")
                    }
                },
                selected = false,
                onClick = {},
            )
        }
    }
}
