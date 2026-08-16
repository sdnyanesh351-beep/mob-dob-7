package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.data.AppEnvironment
import com.example.data.JobTraqRepository

@Composable
fun SettingsScreen(
    currentTenant: String,
    currentRole: String,
    currentLanguage: String,
    currentEnvironment: AppEnvironment = AppEnvironment.DEV,
    onTenantSelected: (String) -> Unit,
    onRoleSelected: (String) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onEnvironmentSelected: (AppEnvironment) -> Unit = {},
    darkThemeOverride: Boolean = false,
    onToggleTheme: (() -> Unit)? = null,
    onReplayOnboarding: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    isScrollable: Boolean = true,
    syncState: JobTraqRepository.SyncState = JobTraqRepository.SyncState.SYNCED,
    platformSettings: JobTraqRepository.PlatformSettings = JobTraqRepository.PlatformSettings(),
    dataSaverEnabled: Boolean = false,
    onToggleDataSaver: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isBiometricsActive by remember { mutableStateOf(true) }
    var is2FAEnabled by remember { mutableStateOf(false) }

    val baseModifier = if (isScrollable) {
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen_root")
    } else {
        modifier
            .fillMaxWidth()
            .testTag("settings_screen_root")
    }

    Column(
        modifier = baseModifier
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Application Settings",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "Manage multi-tenant workspace, user roles & localization",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 0. ENVIRONMENT PROFILE SECTION (TEST, DEV, PROD)
        SettingsSectionHeader(
            icon = Icons.Default.Settings,
            title = "Environment Profile",
            subtitle = "Current Active Profile: ${currentEnvironment.badgeTag}"
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppEnvironment.values().forEach { env ->
            val isSelected = currentEnvironment == env
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onEnvironmentSelected(env) }
                    .testTag("env_profile_chip_${env.keyName.lowercase()}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = env.keyName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = env.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            if (env == AppEnvironment.DEV) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "ACTIVE PROFILE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = env.description,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 1. TENANT / ORGANIZATION SWITCHER SECTION
        SettingsSectionHeader(
            icon = Icons.Default.Business,
            title = "Multi-Tenant Workspace",
            subtitle = "Select active organization workspace environment"
        )

        Spacer(modifier = Modifier.height(10.dp))

        val tenants = listOf(
            Triple("platform", "JobTraq Platform", "Default platform environment with full global workspace access"),
            Triple("acme", "Acme Corp Tenant", "Enterprise multi-tenant space configured for Acme Corporation"),
            Triple("global", "Global Institute Tenant", "Academic & institutional workspace for Global Institute")
        )

        tenants.forEach { (key, name, description) ->
            val isSelected = currentTenant == key
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onTenantSelected(key) }
                    .testTag("tenant_switcher_chip_$key"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. USER ACCESS ROLE SELECTOR SECTION
        SettingsSectionHeader(
            icon = Icons.Default.Person,
            title = "User Access Role",
            subtitle = "Control feature visibility and administrative privileges"
        )

        Spacer(modifier = Modifier.height(10.dp))

        val roles = listOf(
            Triple("User", "Standard Candidate", "View job applications, practice interview prep, and manage profile"),
            Triple("Manager", "Hiring Manager", "Manage team applications, review candidate pipelines, and approve slots"),
            Triple("Admin", "Platform Administrator", "Full system permissions, tenant configurations, and security logs")
        )

        roles.forEach { (roleKey, roleTitle, roleDesc) ->
            val isSelected = currentRole.equals(roleKey, ignoreCase = true)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onRoleSelected(roleKey) }
                    .testTag("role_switcher_chip_$roleKey"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary) else null
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$roleTitle ($roleKey)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = roleDesc,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. LANGUAGE TOGGLE & LOCALIZATION SECTION
        SettingsSectionHeader(
            icon = Icons.Default.Language,
            title = "Display Language & Localization",
            subtitle = "Switch application locale and internationalization strings"
        )

        Spacer(modifier = Modifier.height(10.dp))

        val languages = listOf(
            Triple("en", "English", "EN • Default locale"),
            Triple("hi", "हिन्दी (Hindi)", "HI • भारतीय भाषा"),
            Triple("mr", "मराठी (Marathi)", "MR • प्रादेशिक भाषा")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            languages.forEach { (code, label, sub) ->
                val isSelected = currentLanguage == code
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onLanguageSelected(code) }
                        .testTag("language_switcher_chip_$code"),
                    color = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = code.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. SYSTEM & SECURITY PREFERENCES
        SettingsSectionHeader(
            icon = Icons.Default.Security,
            title = "Security & Theme Preferences",
            subtitle = "Local authentication and dark appearance options"
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsToggleRow(
                    icon = Icons.Default.Fingerprint,
                    label = "Biometric Lock",
                    description = "Enable fingerprint / face scan for session unlocking",
                    checked = isBiometricsActive,
                    onCheckedChange = { isBiometricsActive = it },
                    testTag = "biometric_toggle"
                )

                Spacer(modifier = Modifier.height(14.dp))

                SettingsToggleRow(
                    icon = Icons.Default.Security,
                    label = "Two-Factor Verification",
                    description = "Prompt OTP authentication code on unfamiliar devices",
                    checked = is2FAEnabled,
                    onCheckedChange = { is2FAEnabled = it },
                    testTag = "2fa_toggle"
                )

                Spacer(modifier = Modifier.height(14.dp))

                SettingsToggleRow(
                    icon = Icons.Default.Cloud,
                    label = "Data Saver Mode",
                    description = "Hide blog images to reduce network data usage",
                    checked = dataSaverEnabled,
                    onCheckedChange = onToggleDataSaver,
                    testTag = "data_saver_toggle"
                )

                if (onToggleTheme != null) {
                    Spacer(modifier = Modifier.height(14.dp))

                    SettingsToggleRow(
                        icon = Icons.Default.DarkMode,
                        label = "Dark Mode Interface",
                        description = "Override system layout theme to dark mode",
                        checked = darkThemeOverride,
                        onCheckedChange = { onToggleTheme() },
                        testTag = "theme_toggle_switch"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. REMOTE SYNCHRONIZED POLICIES
        SettingsSectionHeader(
            icon = Icons.Default.Security,
            title = "Synchronized Policies",
            subtitle = "Active platform policies synced from Web App"
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth().testTag("sync_policies_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Sync Status Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (syncState) {
                                com.example.data.JobTraqRepository.SyncState.SYNCED -> Color(0xFFE8F5E9)
                                com.example.data.JobTraqRepository.SyncState.SYNCING -> Color(0xFFFFF3E0)
                                com.example.data.JobTraqRepository.SyncState.OFFLINE_CACHED -> Color(0xFFECEFF1)
                                com.example.data.JobTraqRepository.SyncState.SYNC_ERROR -> Color(0xFFFFEBEE)
                            }
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusText = when (syncState) {
                        com.example.data.JobTraqRepository.SyncState.SYNCED -> "Connected & Synced"
                        com.example.data.JobTraqRepository.SyncState.SYNCING -> "Synchronizing policies..."
                        com.example.data.JobTraqRepository.SyncState.OFFLINE_CACHED -> "Offline (Using Cached Local policies)"
                        com.example.data.JobTraqRepository.SyncState.SYNC_ERROR -> "Sync Connection Failed"
                    }
                    val statusColor = when (syncState) {
                        com.example.data.JobTraqRepository.SyncState.SYNCED -> Color(0xFF2E7D32)
                        com.example.data.JobTraqRepository.SyncState.SYNCING -> Color(0xFFEF6C00)
                        com.example.data.JobTraqRepository.SyncState.OFFLINE_CACHED -> Color(0xFF37474F)
                        com.example.data.JobTraqRepository.SyncState.SYNC_ERROR -> Color(0xFFC62828)
                    }

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Policy Items
                PolicyItemRow(
                    label = "Global AI Copilot Features",
                    enabled = platformSettings.isAIEnabled
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
                
                PolicyItemRow(
                    label = "Community Discussion Feed",
                    enabled = platformSettings.communityFeedEnabled
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                PolicyItemRow(
                    label = "ATS Resume Match Analyzer",
                    enabled = platformSettings.resumeAnalyzerEnabled
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                PolicyItemRow(
                    label = "AI Cover Letter Generator",
                    enabled = platformSettings.coverLetterGeneratorEnabled
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                PolicyItemRow(
                    label = "AI Mock practice interviews",
                    enabled = platformSettings.mockInterviewEnabled
                )
            }
        }

        if (onReplayOnboarding != null) {
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onReplayOnboarding() }
                    .testTag("replay_onboarding_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Product Tour & Onboarding Guide",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Replay animated multi-step feature guide and progress bar overview",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsSectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun PolicyItemRow(
    label: String,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (enabled) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        ) {
            Text(
                text = if (enabled) "ACTIVE" else "LOCKED",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) Color(0xFF2E7D32) else Color(0xFFC62828)
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
