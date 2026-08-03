package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.QuizResult
import com.example.data.UserEntity
import com.example.data.QuestionEntity
import com.example.data.BlogPostEntity
import com.example.data.WalletTransactionEntity
import android.content.Context
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileDashboardScreen(
    user: UserEntity,
    darkThemeOverride: Boolean,
    isEditModalOpen: Boolean,
    onLogout: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenEditModal: () -> Unit,
    onCloseEditModal: () -> Unit,
    onSaveProfile: (String, String, Int) -> Unit,
    recentQuizResults: List<QuizResult> = emptyList(),
    jobsCount: Int = 0,
    interviewsCount: Int = 0,
    offersCount: Int = 0,
    walletCoins: Int = 250,
    streakDays: Int = 5,
    currentTenant: String = "platform",
    currentRole: String = "User",
    currentLanguage: String = "en",
    onTenantSelected: (String) -> Unit = {},
    onRoleSelected: (String) -> Unit = {},
    onLanguageSelected: (String) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenReferrals: () -> Unit = {},
    onReplayOnboarding: (() -> Unit)? = null,
    walletTransactions: List<WalletTransactionEntity> = emptyList(),
    questions: List<QuestionEntity> = emptyList(),
    blogPosts: List<BlogPostEntity> = emptyList(),
    onToggleQuestionBookmark: (String) -> Unit = {},
    onToggleBlogBookmark: (String) -> Unit = {}
) {
    val avatarColors = listOf(
        Color(0xFF4F46E5),
        Color(0xFF7C3AED),
        Color(0xFF06B6D4),
        Color(0xFF10B981),
        Color(0xFFF59E0B)
    )
    val avatarColor = avatarColors.getOrElse(user.avatarBadgeIndex) { MaterialTheme.colorScheme.primary }

    var selectedSectionTab by remember { mutableIntStateOf(0) } // 0: Overview, 1: Skills & Career, 2: Security & Preferences, 3: Bookmarks
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showWalletHistoryDialog by remember { mutableStateOf(false) }

    val ctx = LocalContext.current
    val sharedPrefs = remember(ctx) { ctx.getSharedPreferences("jobtraq_profile_skills", Context.MODE_PRIVATE) }
    var skillsList by remember {
        mutableStateOf(
            sharedPrefs.getString("skills", "Kotlin,Jetpack Compose,Coroutines & Flow,Room DB,Clean Architecture,System Design,REST APIs")
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?: emptyList()
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Hero Profile Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_profile_hero_card"),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    avatarColor,
                                    avatarColor.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.primary
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            // Avatar Circle & Badge
                            Box {
                                Surface(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape),
                                    color = Color.White.copy(alpha = 0.25f),
                                    border = BorderStroke(2.dp, Color.White.copy(alpha = 0.6f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = user.fullName.take(1).uppercase(),
                                            style = MaterialTheme.typography.headlineLarge.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        )
                                    }
                                }
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(24.dp)
                                        .clip(CircleShape),
                                    color = Color(0xFF10B981)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Active Status",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Action Buttons: Edit Profile
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.clickable { onOpenEditModal() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Edit Profile",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.fullName,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Account",
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Role & Member Since Badges
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "⚡ $currentRole",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            val formattedDate = remember(user.createdAt) {
                                val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                                sdf.format(Date(user.createdAt))
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Joined $formattedDate",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.9f)
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Career Stats Quick Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Work,
                    iconTint = MaterialTheme.colorScheme.primary,
                    value = "$jobsCount",
                    label = "Jobs Tracked",
                    subLabel = "$interviewsCount Interviews"
                )

                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.LocalFireDepartment,
                    iconTint = Color(0xFFEF4444),
                    value = "$streakDays Days",
                    label = "Daily Streak",
                    subLabel = "Active Seeker"
                )

                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.MonetizationOn,
                    iconTint = Color(0xFFF59E0B),
                    value = "$walletCoins",
                    label = "JobCoins",
                    subLabel = "Rewards Balance",
                    onClick = { showWalletHistoryDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive Tab Navigation inside Profile Page
            TabRow(
                selectedTabIndex = selectedSectionTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(RoundedCornerShape(16.dp))
            ) {
                Tab(
                    selected = selectedSectionTab == 0,
                    onClick = { selectedSectionTab = 0 },
                    text = { Text("Account", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedSectionTab == 1,
                    onClick = { selectedSectionTab = 1 },
                    text = { Text("Skills", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    icon = { Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedSectionTab == 2,
                    onClick = { selectedSectionTab = 2 },
                    text = { Text("Security", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedSectionTab == 3,
                    onClick = { selectedSectionTab = 3 },
                    text = { Text("Saved", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedSectionTab) {
                0 -> {
                    // Account Overview Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "PERSONAL DETAILS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            DetailRow(
                                icon = Icons.Default.Badge,
                                label = "Account Identifier",
                                value = "AUTH-${user.id}"
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                            DetailRow(
                                icon = Icons.Default.Email,
                                label = "Email Address",
                                value = user.email
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                            DetailRow(
                                icon = Icons.Default.Phone,
                                label = "Phone Number",
                                value = user.phone.ifBlank { "Not provided (click Edit to add)" }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                            DetailRow(
                                icon = Icons.Default.Business,
                                label = "Active Tenant / Organization",
                                value = currentTenant.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                            )
                        }
                    }
                }

                1 -> {
                    // Career & Skills Profile Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "CAREER & SKILLS SNAPSHOT",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            DetailRow(
                                icon = Icons.Default.Work,
                                label = "Target Job Role",
                                value = "Senior Software Engineer / Tech Lead"
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "CORE COMPETENCIES & TAGS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                skillsList.forEach { skill ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = skill,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Delete skill",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable {
                                                        skillsList = skillsList - skill
                                                        sharedPrefs.edit().putString("skills", skillsList.joinToString(",")).apply()
                                                    }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            var newSkillText by remember { mutableStateOf("") }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newSkillText,
                                    onValueChange = { newSkillText = it },
                                    label = { Text("Add Skill Tag") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (newSkillText.isNotBlank()) {
                                            val trimmed = newSkillText.trim()
                                            if (!skillsList.contains(trimmed)) {
                                                skillsList = skillsList + trimmed
                                                sharedPrefs.edit().putString("skills", skillsList.joinToString(",")).apply()
                                            }
                                            newSkillText = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Add")
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Security & Preferences
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "SECURITY & INTERFACE MODE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            ToggleRow(
                                icon = Icons.Default.DarkMode,
                                label = "Dark Theme Override",
                                description = "Force dark mode interface styling",
                                checked = darkThemeOverride,
                                onCheckedChange = { onToggleTheme() },
                                testTag = "profile_dark_mode_switch"
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                            ToggleRow(
                                icon = Icons.Default.Fingerprint,
                                label = "Biometric Lock",
                                description = "Enable fingerprint / face unlock on launch",
                                checked = user.isBiometricsEnabled,
                                onCheckedChange = { /* toggle bio */ },
                                testTag = "profile_biometric_switch"
                            )
                        }
                    }
                }
                3 -> {
                    BookmarksTabContent(
                        questions = questions,
                        blogPosts = blogPosts,
                        onToggleQuestionBookmark = onToggleQuestionBookmark,
                        onToggleBlogBookmark = onToggleBlogBookmark
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Shortcut Actions
            Text(
                text = "PROGRAMS & SHORTCUTS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Referrals & Rewards Program Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenReferrals() }
                    .testTag("profile_referrals_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = "Referrals Program",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Referrals & Rewards Program 🎁",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            Text(
                                text = "Invite friends, track leaderboard & earn JobCoins",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            )
                        }
                    }

                    Button(
                        onClick = onOpenReferrals,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("open_referrals_button")
                    ) {
                        Text("Open 🎁")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Settings Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSettings() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "App & Environment Settings",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = "Tenant ($currentTenant), Language (${currentLanguage.uppercase()})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onOpenSettings,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("open_settings_page_button")
                    ) {
                        Text("Configure")
                    }
                }
            }

            if (onReplayOnboarding != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onReplayOnboarding() }
                        .testTag("profile_onboarding_tour_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.RocketLaunch,
                                        contentDescription = "Replay Tour",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Product Tour Guide",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Replay onboarding overview & tips",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        TextButton(
                            onClick = onReplayOnboarding,
                            modifier = Modifier.testTag("profile_replay_tour_button")
                        ) {
                            Text("Replay Tour")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Sign Out Button
            Button(
                onClick = { showLogoutConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("logout_button"),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Sign Out",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign Out Safely",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Logout Confirmation Dialog
        if (showLogoutConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmDialog = false },
                title = { Text("Sign Out", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to sign out of your JobTraq account?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutConfirmDialog = false
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Sign Out")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Edit Profile Modal
        if (isEditModalOpen) {
            EditProfileDialog(
                currentUser = user,
                onDismiss = onCloseEditModal,
                onSave = onSaveProfile
            )
        }

        // Wallet History Dialog
        if (showWalletHistoryDialog) {
            Dialog(onDismissRequest = { showWalletHistoryDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "JobCoins Wallet History",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Current Balance: $walletCoins Coins",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (walletTransactions.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No transactions found.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .heightIn(max = 280.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                walletTransactions.forEach { tx ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = tx.description,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(tx.timestamp)),
                                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                                            )
                                        }
                                        val isCredit = tx.type.equals("credit", ignoreCase = true) || tx.amount >= 0
                                        Text(
                                            text = (if (isCredit) "+" else "") + "${tx.amount}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCredit) Color(0xFF166534) else Color(0xFF991B1B)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            TextButton(onClick = { showWalletHistoryDialog = false }) {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarksTabContent(
    questions: List<QuestionEntity>,
    blogPosts: List<BlogPostEntity>,
    onToggleQuestionBookmark: (String) -> Unit,
    onToggleBlogBookmark: (String) -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) } // 0: Questions, 1: Blogs

    val bookmarkedQuestions = remember(questions) { questions.filter { it.isBookmarked } }
    val bookmarkedBlogs = remember(blogPosts) { blogPosts.filter { it.bookmarkedBy.isNotEmpty() } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        TabRow(
            selectedTabIndex = subTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = subTab == 0,
                onClick = { subTab = 0 },
                text = { Text("Questions (${bookmarkedQuestions.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = subTab == 1,
                onClick = { subTab = 1 },
                text = { Text("Blogs (${bookmarkedBlogs.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (subTab == 0) {
            if (bookmarkedQuestions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved questions yet.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    bookmarkedQuestions.forEach { question ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = question.category,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = question.questionText,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 3,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { onToggleQuestionBookmark(question.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = "Remove Bookmark",
                                        tint = Color(0xFFFBC02D)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (bookmarkedBlogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No bookmarked blogs yet.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    bookmarkedBlogs.forEach { post ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = post.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "By ${post.author} • ${post.category}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { onToggleBlogBookmark(post.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = "Remove Bookmark",
                                        tint = Color(0xFFFBC02D)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    subLabel: String,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.15f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun ToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
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
private fun EditProfileDialog(
    currentUser: UserEntity,
    onDismiss: () -> Unit,
    onSave: (String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf(currentUser.fullName) }
    var phone by remember { mutableStateOf(currentUser.phone) }
    var selectedAvatarIndex by remember { mutableIntStateOf(currentUser.avatarBadgeIndex) }

    val avatarColors = listOf(
        Color(0xFF4F46E5),
        Color(0xFF7C3AED),
        Color(0xFF06B6D4),
        Color(0xFF10B981),
        Color(0xFFF59E0B)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("edit_profile_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar Color Palette Picker
                Text(
                    text = "AVATAR THEME COLOR",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    avatarColors.forEachIndexed { index, color ->
                        Surface(
                            shape = CircleShape,
                            color = color,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { selectedAvatarIndex = index },
                            border = if (selectedAvatarIndex == index) BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else null
                        ) {
                            if (selectedAvatarIndex == index) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected Avatar",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_name_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_phone_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(name, phone, selectedAvatarIndex)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_profile_button")
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
