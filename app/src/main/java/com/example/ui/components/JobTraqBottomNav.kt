package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.I18nHelper
import com.example.data.JobTraqRepository

enum class JobTraqTab(val icon: ImageVector, val i18nKey: String, val tag: String) {
    PIPELINE(Icons.Default.Work, "pipeline", "tab_pipeline"),
    PREP_HUB(Icons.Default.Psychology, "prep_hub", "tab_prep"),
    COMMUNITY(Icons.Default.Forum, "community", "tab_community"),
    BLOG(Icons.Default.Book, "blog", "tab_blog"),
    REFERRALS(Icons.Default.CardGiftcard, "referrals", "tab_referrals"),
    TOOLS(Icons.Default.AutoAwesome, "tools", "tab_tools"),
    PROFILE(Icons.Default.AccountCircle, "profile", "tab_profile")
}

@Composable
fun JobTraqBottomNav(
    selectedTab: JobTraqTab,
    currentLanguage: String,
    platformSettings: JobTraqRepository.PlatformSettings,
    onTabSelected: (JobTraqTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.height(60.dp),
        windowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 6.dp
    ) {
        val visibleTabs = JobTraqTab.entries.filter { tab ->
            when (tab) {
                JobTraqTab.REFERRALS -> false
                JobTraqTab.BLOG -> false
                JobTraqTab.COMMUNITY -> platformSettings.communityFeedEnabled
                JobTraqTab.TOOLS -> platformSettings.isAIEnabled
                else -> true
            }
        }
        visibleTabs.forEach { tab ->
            val label = I18nHelper.getString(tab.i18nKey, currentLanguage)
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.testTag(tab.tag)
            )
        }
    }
}

