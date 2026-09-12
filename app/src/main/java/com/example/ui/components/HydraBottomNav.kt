package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.HydraBgDark
import com.example.ui.theme.HydraBorder
import com.example.ui.theme.HydraCyan
import com.example.ui.theme.HydraPurple
import com.example.ui.theme.HydraSurface
import com.example.ui.theme.HydraTextMuted

@Composable
fun HydraBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = HydraSurface,
        modifier = modifier
            .border(width = 1.dp, color = HydraBorder)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Guided
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.testTag("tab_guided"),
            icon = {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = stringResource(R.string.guided_mode),
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.guided_mode),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HydraCyan,
                selectedTextColor = HydraCyan,
                indicatorColor = HydraBgDark,
                unselectedIconColor = HydraTextMuted,
                unselectedTextColor = HydraTextMuted
            )
        )

        // Console
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.testTag("tab_console"),
            icon = {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = stringResource(R.string.console_mode),
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.console_mode),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HydraPurple,
                selectedTextColor = HydraPurple,
                indicatorColor = HydraBgDark,
                unselectedIconColor = HydraTextMuted,
                unselectedTextColor = HydraTextMuted
            )
        )

        // Graph
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            modifier = Modifier.testTag("tab_graph"),
            icon = {
                Icon(
                    imageVector = Icons.Default.Hub,
                    contentDescription = stringResource(R.string.graph_mode),
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.graph_mode),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HydraCyan,
                selectedTextColor = HydraCyan,
                indicatorColor = HydraBgDark,
                unselectedIconColor = HydraTextMuted,
                unselectedTextColor = HydraTextMuted
            )
        )
    }
}
