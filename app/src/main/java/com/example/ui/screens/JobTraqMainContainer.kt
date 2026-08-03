package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.JobEntity
import com.example.data.JobTraqRepository
import com.example.data.UserEntity
import com.example.ui.components.JobTraqBottomNav
import com.example.ui.components.JobTraqTab
import com.example.ui.components.JobTraqTopBar
import com.example.ui.components.HeadsUpNotificationBanner
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.example.data.AuthDatabase

import com.example.data.AppEnvironment
import com.example.data.SessionManager
import com.example.data.StreakData
import com.example.data.StreakDataStoreManager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Switch
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.I18nHelper

@Composable
fun JobTraqMainContainer(
    user: UserEntity,
    darkThemeOverride: Boolean,
    isEditModalOpen: Boolean,
    onLogout: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenEditModal: () -> Unit,
    onCloseEditModal: () -> Unit,
    onSaveProfile: (String, String, Int) -> Unit,
    onReplayOnboarding: (() -> Unit)? = null,
    initialTenant: String = "platform",
    activeEnvironment: AppEnvironment = AppEnvironment.DEV,
    onEnvironmentSelected: (AppEnvironment) -> Unit = {},
    baseUrl: String = AppEnvironment.DEV.defaultBaseUrl,
    sessionManager: SessionManager? = null,
    repository: JobTraqRepository = remember(sessionManager) { JobTraqRepository(sessionManager) }
) {
    var selectedTab by remember { mutableStateOf(JobTraqTab.PIPELINE) }
    var isQuizActive by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val context = LocalContext.current
    val interviewDao = remember(context) { AuthDatabase.getDatabase(context).interviewDao() }
    val streakDataStoreManager = remember(context) { StreakDataStoreManager(context) }

    val streakData by streakDataStoreManager.streakDataFlow.collectAsStateWithLifecycle(initialValue = StreakData())

    LaunchedEffect(Unit) {
        streakDataStoreManager.recordDailyLogin()
    }

    LaunchedEffect(initialTenant, baseUrl, activeEnvironment) {
        if (initialTenant.isNotBlank()) {
            repository.setTenant(initialTenant)
        }
        repository.setEnvironment(activeEnvironment)
        if (baseUrl.isNotBlank()) {
            repository.setBaseUrl(baseUrl)
            if (activeEnvironment != AppEnvironment.TEST) {
                repository.syncAllDataFromApi(baseUrl)
            }
        }
    }

    val currentTenant by repository.currentTenant.collectAsStateWithLifecycle()
    val currentRole by repository.currentRole.collectAsStateWithLifecycle()
    val currentLanguage by repository.currentLanguage.collectAsStateWithLifecycle()

    val jobs by repository.jobs.collectAsStateWithLifecycle()
    val questions by repository.questions.collectAsStateWithLifecycle()
    val quizzes by repository.quizzes.collectAsStateWithLifecycle()
    val recentQuizResults by repository.recentQuizResults.collectAsStateWithLifecycle()
    val feedPosts by repository.feedPosts.collectAsStateWithLifecycle()
    val walletState by repository.walletState.collectAsStateWithLifecycle()
    val resumes by repository.resumes.collectAsStateWithLifecycle()
    val offers by repository.offers.collectAsStateWithLifecycle()
    val alumniMentors by repository.alumniMentors.collectAsStateWithLifecycle()
    val blogPosts by repository.blogPosts.collectAsStateWithLifecycle()
    val coverLetters by repository.coverLetters.collectAsStateWithLifecycle()
    val challenges by repository.challenges.collectAsStateWithLifecycle()
    val attempts by repository.attempts.collectAsStateWithLifecycle()
    val dailyStreakState by repository.dailyStreakState.collectAsStateWithLifecycle()
    val resumeScanHistory by repository.resumeScanHistory.collectAsStateWithLifecycle()
    val referrals by repository.referrals.collectAsStateWithLifecycle()
    val referralLeaderboard by repository.referralLeaderboard.collectAsStateWithLifecycle()
    val referralActivityLogs by repository.referralActivityLogs.collectAsStateWithLifecycle()

    var isSettingsScreenOpen by remember { mutableStateOf(false) }
    var isReferralsScreenOpen by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        if (selectedTab != JobTraqTab.PREP_HUB) {
            isQuizActive = false
        }
    }

    fun showToast(msg: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(msg)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isQuizActive && !isSettingsScreenOpen && !isReferralsScreenOpen,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.testTag("nav_drawer_sheet"),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                ) {
                    // Header inside Nav Drawer
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "JobTraq",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "JobTraq",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "career simplified",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // User Profile Card inside Drawer
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.testTag("drawer_user_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = user.fullName.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = user.fullName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Drawer Items
                    JobTraqTab.entries.forEach { tab ->
                        val label = I18nHelper.getString(tab.i18nKey, currentLanguage)
                        val isSelected = selectedTab == tab && !isSettingsScreenOpen && !isReferralsScreenOpen
                        val itemTag = if (tab == JobTraqTab.BLOG) "drawer_blog_item" else "drawer_item_${tab.name.lowercase()}"

                        NavigationDrawerItem(
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (tab == JobTraqTab.BLOG) "$label 📰" else label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 15.sp
                                    )
                                    if (tab == JobTraqTab.BLOG) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(start = 4.dp)
                                        ) {
                                            Text(
                                                text = "NEW",
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
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = label,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                selectedTab = tab
                                isSettingsScreenOpen = false
                                isReferralsScreenOpen = false
                                coroutineScope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .testTag(itemTag)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Global Theme Toggle
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("drawer_theme_toggle")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleTheme() }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (darkThemeOverride) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = "Theme Toggle",
                                    tint = if (darkThemeOverride) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (darkThemeOverride) "Dark Theme" else "Light Theme",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (darkThemeOverride) "Switch to Light Mode" else "Switch to Dark Mode",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = darkThemeOverride,
                                onCheckedChange = { onToggleTheme() },
                                modifier = Modifier.testTag("drawer_theme_switch")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Settings Drawer Action
                    NavigationDrawerItem(
                        label = { Text("Settings", fontSize = 15.sp) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        selected = isSettingsScreenOpen,
                        onClick = {
                            isSettingsScreenOpen = true
                            isReferralsScreenOpen = false
                            coroutineScope.launch { drawerState.close() }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .testTag("drawer_item_settings")
                    )

                    // Logout Drawer Action
                    NavigationDrawerItem(
                        label = { Text("Sign Out", fontSize = 15.sp, color = MaterialTheme.colorScheme.error) },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Sign Out",
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            onLogout()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .testTag("drawer_item_logout")
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (!isQuizActive && !isSettingsScreenOpen && !isReferralsScreenOpen) {
                    JobTraqTopBar(
                        currentLanguage = currentLanguage,
                        activeEnvironment = activeEnvironment,
                        streakDays = streakData.streakDays,
                        onOpenSettings = { isSettingsScreenOpen = true },
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                    )
                }
            },
            bottomBar = {
                if (!isQuizActive && !isSettingsScreenOpen && !isReferralsScreenOpen) {
                    JobTraqBottomNav(
                        selectedTab = selectedTab,
                        currentLanguage = currentLanguage,
                        onTabSelected = { selectedTab = it }
                    )
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isQuizActive || isSettingsScreenOpen || isReferralsScreenOpen) PaddingValues(0.dp) else innerPadding)
            ) {
                if (isSettingsScreenOpen) {
                    SettingsScreen(
                        currentTenant = currentTenant,
                        currentRole = currentRole,
                        currentLanguage = currentLanguage,
                        currentEnvironment = activeEnvironment,
                        onTenantSelected = { repository.setTenant(it) },
                        onRoleSelected = { repository.setRole(it) },
                        onLanguageSelected = { repository.setLanguage(it) },
                        onEnvironmentSelected = {
                            onEnvironmentSelected(it)
                            repository.setEnvironment(it)
                        },
                        darkThemeOverride = darkThemeOverride,
                        onToggleTheme = onToggleTheme,
                        onReplayOnboarding = onReplayOnboarding,
                        onBack = { isSettingsScreenOpen = false }
                    )
                } else if (isReferralsScreenOpen) {
                    Phase5ReferralsScreen(
                        referrals = referrals,
                        leaderboard = referralLeaderboard,
                        activityLogs = referralActivityLogs,
                        walletState = walletState,
                        onCreateReferral = { email, code, dept ->
                            repository.createReferral(email, code, dept)
                        },
                        onActivateReferral = { id ->
                            repository.activateReferral(id)
                        },
                        onMarkReferralHired = { id, dept ->
                            repository.markReferralHired(id, dept)
                        },
                        onNudgeReferralFriend = { id ->
                            repository.nudgeReferralFriend(id)
                        },
                        onGiftStreakFreeze = { id ->
                            repository.giftStreakFreeze(id)
                        },
                        onShowToast = { showToast(it) },
                        onBack = { isReferralsScreenOpen = false }
                    )
                } else {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "JobTraqTabTransition"
                    ) { tab ->
                        when (tab) {
                            JobTraqTab.PIPELINE -> {
                                Phase1JobTrackerScreen(
                                    jobs = jobs,
                                    offers = offers,
                                    currentTenant = currentTenant,
                                    currentLanguage = currentLanguage,
                                    baseUrl = baseUrl,
                                    isDummyDataAllowed = activeEnvironment.isDummyDataAllowed,
                                    sessionManager = sessionManager,
                                    recentQuizResults = recentQuizResults,
                                    streakData = streakData,
                                    onAddJob = { newJob ->
                                        repository.addJob(newJob)
                                        showToast("Job application saved!")
                                    },
                                    onUpdateStatus = { id, status ->
                                        repository.updateJobStatus(id, status)
                                        showToast("Status updated to '$status'")
                                    },
                                    onDeleteJob = { id ->
                                        repository.deleteJob(id)
                                        showToast("Job deleted from pipeline.")
                                    },
                                    onScheduleReminder = { company ->
                                        showToast("Reminder scheduled for $company interview!")
                                    },
                                    onAddOffer = { offer ->
                                        repository.addOffer(offer)
                                        showToast("Offer for ${offer.companyName} added!")
                                    },
                                    onDeleteOffer = { id ->
                                        repository.deleteOffer(id)
                                        showToast("Offer removed.")
                                    }
                                )
                            }

                            JobTraqTab.PREP_HUB -> {
                                Phase2InterviewPrepScreen(
                                    questions = questions,
                                    quizzes = quizzes,
                                    currentLanguage = currentLanguage,
                                    recentQuizResults = recentQuizResults,
                                    challenges = challenges,
                                    attempts = attempts,
                                    dailyStreakState = dailyStreakState,
                                    onCompleteDailyChallenge = { xp -> repository.completeDailyChallenge(xp) },
                                    onUpdateNotificationSettings = { enabled, time, freq, sound, vibrate ->
                                        repository.updateNotificationSettings(enabled, time, freq, sound, vibrate)
                                    },
                                    onTriggerTestNotification = { repository.triggerTestNotification() },
                                    onSaveQuizResult = { repository.saveQuizResult(it) },
                                    onToggleBookmark = { id -> repository.toggleBookmark(id) },
                                    onCreateQuiz = { title, desc, qList ->
                                        repository.createQuiz(title, desc, qList)
                                        showToast("Custom quiz '$title' created!")
                                    },
                                    onCreateChallenge = { result, creator, friends ->
                                        val chal = repository.createQuizChallenge(result, creator, friends)
                                        showToast("Multiplayer Challenge code ${chal.code} generated!")
                                        chal
                                    },
                                    onSubmitAttempt = { code, participant, score, correct, total, duration ->
                                        val att = repository.submitQuizAttempt(code, participant, score, correct, total, duration)
                                        att
                                    },
                                    onAnalyzeAnswerWithAI = { qText, aText ->
                                        repository.analyzeAnswerWithAI(qText, aText)
                                    },
                                    onQuizStateChanged = { active -> isQuizActive = active },
                                    onShareToCommunity = { text -> repository.addFeedPost(text, user.fullName) },
                                    onSchedulePracticeInterview = { scheduledInterview ->
                                        coroutineScope.launch {
                                            val tenant = currentTenant.ifBlank { "platform" }
                                            interviewDao.insertOrUpdate(scheduledInterview.copy(tenantId = tenant))
                                        }
                                    },
                                    onShowToast = { showToast(it) }
                                )
                            }

                            JobTraqTab.COMMUNITY -> {
                                Phase3CommunityScreen(
                                    feedPosts = feedPosts,
                                    walletState = walletState,
                                    alumniMentors = alumniMentors,
                                    currentTenant = currentTenant,
                                    onRefresh = {
                                        coroutineScope.launch {
                                            repository.syncAllDataFromApi(baseUrl)
                                        }
                                    },
                                    onAddPost = { title, content, type, pollOpts, evTitle, evDate, evLoc, cap ->
                                        repository.addFeedPost(
                                            title = title,
                                            content = content,
                                            type = type,
                                            pollOptions = pollOpts,
                                            eventTitle = evTitle,
                                            eventDate = evDate,
                                            eventLocation = evLoc,
                                            capacity = cap,
                                            authorName = user.fullName
                                        )
                                    },
                                    onToggleLike = { id -> repository.toggleLikePost(id) },
                                    onAddComment = { id, comment -> repository.addCommentToPost(id, comment) },
                                    onShowToast = { showToast(it) }
                                )
                            }

                            JobTraqTab.BLOG -> {
                                Phase6BlogScreen(
                                    blogPosts = blogPosts,
                                    onRefresh = {
                                        coroutineScope.launch {
                                            repository.syncAllDataFromApi(baseUrl)
                                        }
                                    },
                                    onCreateBlogPost = { title, content, excerpt, tags, imageUrl ->
                                        coroutineScope.launch {
                                            val result = repository.createBlogPost(title, content, excerpt, tags, imageUrl)
                                            showToast(result.second)
                                        }
                                    },
                                    onToggleBookmark = { postId ->
                                        coroutineScope.launch {
                                            val result = repository.toggleBookmarkBlogPost(postId)
                                            showToast(result.second)
                                        }
                                    }
                                )
                            }

                            JobTraqTab.TOOLS -> {
                                Phase4AdvancedToolsScreen(
                                    resumes = resumes,
                                    scanHistory = resumeScanHistory,
                                    coverLetters = coverLetters,
                                    onAddResume = { title, role, content ->
                                        repository.addResume(title, role, content)
                                        showToast("Resume '$title' saved!")
                                    },
                                    onAnalyzeResume = { resId, jd ->
                                        repository.analyzeResumeWithAI(resId, jd)
                                    },
                                    onGenerateCoverLetter = { comp, title, jd, resText ->
                                        repository.generateCoverLetterWithAI(comp, title, jd, resText)
                                    },
                                    onShowToast = { showToast(it) }
                                )
                            }

                            JobTraqTab.REFERRALS -> {
                                Phase5ReferralsScreen(
                                    referrals = referrals,
                                    leaderboard = referralLeaderboard,
                                    activityLogs = referralActivityLogs,
                                    walletState = walletState,
                                    onCreateReferral = { email, code, dept ->
                                        repository.createReferral(email, code, dept)
                                    },
                                    onActivateReferral = { id ->
                                        repository.activateReferral(id)
                                    },
                                    onMarkReferralHired = { id, dept ->
                                        repository.markReferralHired(id, dept)
                                    },
                                    onNudgeReferralFriend = { id ->
                                        repository.nudgeReferralFriend(id)
                                    },
                                    onGiftStreakFreeze = { id ->
                                        repository.giftStreakFreeze(id)
                                    },
                                    onShowToast = { showToast(it) }
                                )
                            }


                            JobTraqTab.PROFILE -> {
                                ProfileDashboardScreen(
                                    user = user,
                                    darkThemeOverride = darkThemeOverride,
                                    isEditModalOpen = isEditModalOpen,
                                    onLogout = onLogout,
                                    onToggleTheme = onToggleTheme,
                                    onOpenEditModal = onOpenEditModal,
                                    onCloseEditModal = onCloseEditModal,
                                    onSaveProfile = onSaveProfile,
                                    recentQuizResults = recentQuizResults,
                                    jobsCount = jobs.size,
                                    interviewsCount = jobs.count { it.interviewDate != null },
                                    offersCount = offers.size,
                                    walletCoins = walletState.coins,
                                    streakDays = dailyStreakState.streakDays,
                                    currentTenant = currentTenant,
                                    currentRole = currentRole,
                                    currentLanguage = currentLanguage,
                                    onTenantSelected = { repository.setTenant(it) },
                                    onRoleSelected = { repository.setRole(it) },
                                    onLanguageSelected = { repository.setLanguage(it) },
                                    onOpenSettings = { isSettingsScreenOpen = true },
                                    onOpenReferrals = { isReferralsScreenOpen = true },
                                    onReplayOnboarding = onReplayOnboarding,
                                    walletTransactions = walletState.transactions,
                                    questions = questions,
                                    blogPosts = blogPosts,
                                    onToggleQuestionBookmark = { qId -> repository.toggleBookmark(qId) },
                                    onToggleBlogBookmark = { blogId ->
                                        coroutineScope.launch {
                                            repository.toggleBookmarkBlogPost(blogId)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                if (dailyStreakState.showSystemNotificationBanner) {
                    HeadsUpNotificationBanner(
                        text = dailyStreakState.notificationBannerText,
                        onActionClick = {
                            selectedTab = JobTraqTab.PREP_HUB
                        },
                        onDismiss = {
                            repository.dismissNotificationBanner()
                        }
                    )
                }
            }
        }
    }
}
