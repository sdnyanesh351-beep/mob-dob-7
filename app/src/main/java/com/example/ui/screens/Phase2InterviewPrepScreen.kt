package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import java.util.UUID
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.I18nHelper
import com.example.data.QuestionEntity
import com.example.data.QuizEntity
import com.example.data.QuizResult
import com.example.data.QuizChallengeEntity
import com.example.data.QuizAttemptEntity
import com.example.data.InterviewEntity
import com.example.ui.components.RecentQuizzesSection

import com.example.data.MockAnswerAnalysis

import com.example.data.DailyStreakNotificationState
import com.example.ui.components.DailyStreakPromptBanner
import com.example.ui.components.DailyNotificationSettingsDialog

@Composable
fun Phase2InterviewPrepScreen(
    questions: List<QuestionEntity>,
    quizzes: List<QuizEntity>,
    currentLanguage: String,
    recentQuizResults: List<QuizResult> = emptyList(),
    challenges: List<QuizChallengeEntity> = emptyList(),
    attempts: List<QuizAttemptEntity> = emptyList(),
    dailyStreakState: DailyStreakNotificationState = DailyStreakNotificationState(),
    onCompleteDailyChallenge: (Int) -> Unit = {},
    onUpdateNotificationSettings: (Boolean, String, String, Boolean, Boolean) -> Unit = { _, _, _, _, _ -> },
    onTriggerTestNotification: () -> Unit = {},
    onSaveQuizResult: (QuizResult) -> Unit = {},
    onToggleBookmark: (String) -> Unit,
    onCreateQuiz: (String, String, List<QuestionEntity>) -> Unit,
    onCreateChallenge: ((QuizResult, String, List<String>) -> QuizChallengeEntity)? = null,
    onSubmitAttempt: ((String, String, Int, Int, Int, Int) -> QuizAttemptEntity)? = null,
    onAnalyzeAnswerWithAI: suspend (String, String) -> MockAnswerAnalysis = { _, _ ->
        MockAnswerAnalysis(88, "", "", "", "", emptyList(), emptyList(), "")
    },
    onQuizStateChanged: (Boolean) -> Unit = {},
    onShareToCommunity: ((String) -> Unit)? = null,
    onSchedulePracticeInterview: (InterviewEntity) -> Unit = {},
    onShowToast: (String) -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Questions, 1: Quizzes, 2: Quiz History & Challenges 🏆
    val subTabs = listOf("Question Bank", "Practice Quizzes", "Quiz History & Challenges 🏆")

    var activeQuiz by remember { mutableStateOf<QuizEntity?>(null) }
    var pendingQuizForConfig by remember { mutableStateOf<QuizEntity?>(null) }
    var activeQuizIsChallengeMode by remember { mutableStateOf(false) }
    var activeQuizIsFeedbackEnabled by remember { mutableStateOf(true) }
    var activeQuizIsInstantFeedbackEnabled by remember { mutableStateOf(false) }
    var activeQuizTimerMinutes by remember { mutableIntStateOf(10) }
    var activeQuizResult by remember { mutableStateOf<QuizResult?>(null) }
    var currentChallengeCode by remember { mutableStateOf<String?>(null) }
    var showNotificationSettingsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(activeQuiz, activeQuizResult) {
        onQuizStateChanged(activeQuiz != null || activeQuizResult != null)
    }

    if (showNotificationSettingsDialog) {
        DailyNotificationSettingsDialog(
            state = dailyStreakState,
            onDismiss = { showNotificationSettingsDialog = false },
            onUpdateSettings = { enabled, time, freq, sound, vibrate ->
                onUpdateNotificationSettings(enabled, time, freq, sound, vibrate)
                onShowToast("Notification reminder settings updated!")
            },
            onTriggerTestNotification = {
                onTriggerTestNotification()
                onShowToast("Test push notification triggered! Check top of screen.")
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header SubTab Navigation
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                subTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedSubTab == index,
                        onClick = {
                            selectedSubTab = index
                            activeQuiz = null
                            activeQuizResult = null
                        },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.testTag("prep_subtab_$index")
                    )
                }
            }

            // Main Content Area
            when (selectedSubTab) {
                0 -> QuestionBankSection(
                    questions = questions,
                    dailyStreakState = dailyStreakState,
                    onCompleteDailyChallenge = onCompleteDailyChallenge,
                    onOpenNotificationSettings = { showNotificationSettingsDialog = true },
                    onToggleBookmark = onToggleBookmark,
                    onShareToCommunity = onShareToCommunity,
                    onSchedulePracticeInterview = onSchedulePracticeInterview,
                    onShowToast = onShowToast
                )

                1 -> QuizzesSection(
                    quizzes = quizzes,
                    questions = questions,
                    onStartQuiz = { quiz -> 
                        pendingQuizForConfig = quiz 
                    },
                    onCreateQuiz = onCreateQuiz
                )
                2 -> QuizHistoryAndChallengesSection(
                    recentQuizResults = recentQuizResults,
                    challenges = challenges,
                    attempts = attempts,
                    onStartChallenge = { chal ->
                        val quiz = QuizEntity(
                            id = "chal-${chal.code}",
                            title = chal.quizTitle,
                            description = chal.quizDescription,
                            questionCount = chal.questions.size,
                            questions = chal.questions
                        )
                        pendingQuizForConfig = quiz
                    },
                    onShowToast = onShowToast
                )
            }
        }

        // Pre-Quiz Configuration Dialog (Mode & Feedback Settings)
        if (pendingQuizForConfig != null) {
            PreQuizConfigDialog(
                quiz = pendingQuizForConfig!!,
                onDismiss = { pendingQuizForConfig = null },
                onStartQuiz = { isChallenge, isFeedback, isInstantFeedback, timerMins ->
                    activeQuizIsChallengeMode = isChallenge
                    activeQuizIsFeedbackEnabled = isFeedback
                    activeQuizIsInstantFeedbackEnabled = isInstantFeedback
                    activeQuizTimerMinutes = timerMins
                    currentChallengeCode = if (isChallenge) "CHAL-${(100000..999999).random()}" else null
                    activeQuiz = pendingQuizForConfig
                    pendingQuizForConfig = null
                }
            )
        }

        // Full Screen Practice Quiz Container (Absolute full-screen positioning in container)
        if (activeQuizResult != null) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("full_screen_quiz_result"),
                color = MaterialTheme.colorScheme.background
            ) {
                QuizResultView(
                    result = activeQuizResult!!,
                    onDismiss = { 
                        activeQuizResult = null 
                        currentChallengeCode = null
                    },
                    onCreateChallenge = onCreateChallenge,
                    onShareToCommunity = onShareToCommunity,
                    onShowToast = { onShowToast(it) }
                )
            }
        } else if (activeQuiz != null) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("full_screen_quiz_player"),
                color = MaterialTheme.colorScheme.background
            ) {
                QuizPlayerView(
                    quiz = activeQuiz!!,
                    isChallengeMode = activeQuizIsChallengeMode,
                    isFeedbackEnabled = activeQuizIsFeedbackEnabled,
                    initialInstantFeedbackEnabled = activeQuizIsInstantFeedbackEnabled,
                    initialTimerMinutes = activeQuizTimerMinutes,
                    onFinishQuiz = { result ->
                        onSaveQuizResult(result)
                        if (!currentChallengeCode.isNullOrBlank() && onSubmitAttempt != null) {
                            onSubmitAttempt(
                                currentChallengeCode!!,
                                "Alex Rivera",
                                result.scorePercentage,
                                result.correctAnswers,
                                result.totalQuestions,
                                result.durationSeconds
                            )
                            onShowToast("Challenge attempt saved! Check the Leaderboard!")
                        }
                        activeQuizResult = result
                        activeQuiz = null
                    },
                    onExit = { 
                        activeQuiz = null
                        currentChallengeCode = null
                    }
                )
            }
        }
    }
}

@Composable
private fun QuestionBankSection(
    questions: List<QuestionEntity>,
    dailyStreakState: DailyStreakNotificationState,
    onCompleteDailyChallenge: (Int) -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onToggleBookmark: (String) -> Unit,
    onShareToCommunity: ((String) -> Unit)? = null,
    onSchedulePracticeInterview: (InterviewEntity) -> Unit,
    onShowToast: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedType by remember { mutableStateOf("All") } // "All", "MCQ", "Q&A"
    var showOnlyBookmarked by remember { mutableStateOf(false) }

    val categories = listOf("All", "Technical", "Behavioral", "System Design", "HR")
    val types = listOf("All", "MCQ", "Q&A")

    val filteredQuestions = remember(questions, searchQuery, selectedCategory, showOnlyBookmarked, selectedType) {
        questions.filter { q ->
            val matchesCategory = selectedCategory == "All" || q.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = q.questionText.contains(searchQuery, ignoreCase = true) || q.category.contains(searchQuery, ignoreCase = true)
            val matchesBookmark = !showOnlyBookmarked || q.isBookmarked
            val matchesType = when (selectedType) {
                "MCQ" -> q.options.isNotEmpty()
                "Q&A" -> q.options.isEmpty()
                else -> true
            }
            matchesCategory && matchesSearch && matchesBookmark && matchesType
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        // Daily Challenge & Streak Building Prompt Banner
        item {
            DailyStreakPromptBanner(
                state = dailyStreakState,
                onCompleteChallenge = onCompleteDailyChallenge,
                onOpenNotificationSettings = onOpenNotificationSettings,
                onShowToast = onShowToast,
                onShareToCommunity = onShareToCommunity
            )
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }

        // Search & Bookmark Toggle Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("question_search_input"),
                    placeholder = { Text("Search questions...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showOnlyBookmarked = !showOnlyBookmarked }
                        .testTag("bookmarked_filter_toggle"),
                    color = if (showOnlyBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = if (showOnlyBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmarked Only",
                        tint = if (showOnlyBookmarked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedCategory = category },
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Question Type Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(types) { type ->
                    val isSelected = selectedType == type
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedType = type },
                        color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }

        // Questions List
        items(filteredQuestions, key = { it.id }) { q ->
            QuestionCardItem(
                question = q,
                onToggleBookmark = onToggleBookmark
            )
        }
        item {
            BookMockInterviewSection(
                onSchedulePracticeInterview = onSchedulePracticeInterview,
                onShowToast = onShowToast
            )
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun QuestionCardItem(
    question: QuestionEntity,
    onToggleBookmark: (String) -> Unit
) {
    var expandedAnswer by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("question_card_${question.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = question.category,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = question.difficulty,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { onToggleBookmark(question.id) },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("bookmark_button_${question.id}")
                ) {
                    Icon(
                        imageVector = if (question.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (question.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = question.questionText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            if (expandedAnswer && question.options.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Multiple Choice Options",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    question.options.forEachIndexed { index, option ->
                        val isCorrect = index == question.correctOptionIndex
                        val optionLetter = ('A' + index).toString()
                        val containerColor = if (isCorrect) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant
                        val contentColor = if (isCorrect) Color(0xFF166534) else MaterialTheme.colorScheme.onSurfaceVariant
                        val borderStroke = if (isCorrect) BorderStroke(1.dp, Color(0xFF15803D)) else null

                        Surface(
                            color = containerColor,
                            contentColor = contentColor,
                            shape = RoundedCornerShape(10.dp),
                            border = borderStroke,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isCorrect) Color(0xFF15803D) else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = optionLetter,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCorrect) Color.White else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                if (isCorrect) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Correct Answer",
                                        tint = Color(0xFF15803D),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { expandedAnswer = !expandedAnswer },
                modifier = Modifier.testTag("toggle_answer_button_${question.id}")
            ) {
                Text(if (expandedAnswer) "Hide Details" else "Show Details")
            }

            if (expandedAnswer) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = question.sampleAnswer,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizzesSection(
    quizzes: List<QuizEntity>,
    questions: List<QuestionEntity>,
    onStartQuiz: (QuizEntity) -> Unit,
    onCreateQuiz: (String, String, List<QuestionEntity>) -> Unit
) {
    var isCreateModalOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Available Quizzes",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Button(
                onClick = { isCreateModalOpen = true },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("create_quiz_button")
            ) {
                Text("Create Custom Quiz")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        quizzes.forEach { quiz ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("quiz_card_${quiz.id}"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = quiz.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = quiz.description,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${quiz.questions.size} Questions",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Button(
                            onClick = { onStartQuiz(quiz) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("start_quiz_button_${quiz.id}")
                        ) {
                            Text("Start Practice")
                        }
                    }
                }
            }
        }
    }

    if (isCreateModalOpen) {
        CreateQuizDialog(
            availableQuestions = questions,
            onDismiss = { isCreateModalOpen = false },
            onCreate = { title, desc, selected ->
                onCreateQuiz(title, desc, selected)
                isCreateModalOpen = false
            }
        )
    }
}

// QuizPlayerView and QuizResultView are now rendered using QuizPlayerComponents.kt


@Composable
private fun BookMockInterviewSection(
    onSchedulePracticeInterview: (InterviewEntity) -> Unit,
    onShowToast: (String) -> Unit
) {
    var showAiDialog by remember { mutableStateOf(false) }
    var showExpertDialog by remember { mutableStateOf(false) }
    var showFriendDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Book Practice Interviews",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Prepare for live interviews with AI audio simulations, expert mentors, or peer friends.",
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // AI Mock Interview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("book_ai_mock_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Practice with Gemini AI",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Instant audio-first interactive mock interview powered by Gemini 3.5 Flash.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showAiDialog = true },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Start Instant AI Session")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Expert Mentor Session Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Practice with Industry Experts",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Schedule a 1-on-1 feedback session with verified tech lead mentors.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showExpertDialog = true },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Request Mentor Session")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Friend Session Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Practice with Friends",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Invite a classmate or peer via email to conduct a peer-to-peer interview.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showFriendDialog = true },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Invite Friend via Link")
                }
            }
        }
    }

    // AI Mock Dialog
    if (showAiDialog) {
        var topic by remember { mutableStateOf("Android Developer") }
        var difficulty by remember { mutableStateOf("Medium") }
        var durationMins by remember { mutableIntStateOf(15) }
        var instantFeedback by remember { mutableStateOf(true) }

        Dialog(onDismissRequest = { showAiDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "AI Mock Interview Setup",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Target Role / Topic") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Difficulty", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        listOf("Easy", "Medium", "Hard").forEach { diff ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = difficulty == diff,
                                    onClick = { difficulty = diff }
                                )
                                Text(diff)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Duration: $durationMins Minutes", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    Slider(
                        value = durationMins.toFloat(),
                        onValueChange = { durationMins = it.toInt() },
                        valueRange = 5f..30f,
                        steps = 4
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Instant Feedback", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Switch(checked = instantFeedback, onCheckedChange = { instantFeedback = it })
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAiDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val scheduledInterview = InterviewEntity(
                                    id = "ai-${UUID.randomUUID()}",
                                    companyName = "Gemini AI Practice",
                                    jobTitle = topic,
                                    status = "Upcoming",
                                    date = "Today",
                                    time = "Now",
                                    location = "In-App Audio Room",
                                    interviewer = "Gemini AI",
                                    notes = "Difficulty: $difficulty. Duration: $durationMins min. Instant Feedback: ${if (instantFeedback) "Enabled" else "Disabled"}",
                                    type = "AI_MOCK"
                                )
                                onSchedulePracticeInterview(scheduledInterview)
                                showAiDialog = false
                                onShowToast("Gemini AI mock interview scheduled!")
                            },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Start Practice")
                        }
                    }
                }
            }
        }
    }

    // Expert booking Dialog
    if (showExpertDialog) {
        var topic by remember { mutableStateOf("Android Tech Lead") }
        var selectedExpert by remember { mutableStateOf("Marcus Vance (VP Engineering)") }
        var date by remember { mutableStateOf("Tomorrow") }
        var time by remember { mutableStateOf("4:00 PM PST") }
        val experts = listOf("Marcus Vance (VP Engineering)", "Sarah Jenkins (Lead Android)", "Elena Rostova (Principal AI)")

        Dialog(onDismissRequest = { showExpertDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Book Expert Mentor Session",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Interview Topic / Role") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Select Mentor", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    experts.forEach { expert ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedExpert = expert }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedExpert == expert,
                                onClick = { selectedExpert = expert }
                            )
                            Text(expert, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time Slot") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showExpertDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val scheduledInterview = InterviewEntity(
                                    id = "exp-${UUID.randomUUID()}",
                                    companyName = "Expert Coaching",
                                    jobTitle = topic,
                                    status = "Pending",
                                    date = date,
                                    time = time,
                                    location = "Google Meet Link",
                                    interviewer = selectedExpert,
                                    notes = "Booked technical session focusing on $topic architecture review.",
                                    type = "EXPERT"
                                )
                                onSchedulePracticeInterview(scheduledInterview)
                                showExpertDialog = false
                                onShowToast("Mentor booking request submitted!")
                            },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Book Session")
                        }
                    }
                }
            }
        }
    }

    // Friend Invitation Dialog
    if (showFriendDialog) {
        var topic by remember { mutableStateOf("Mobile System Design") }
        var friendEmail by remember { mutableStateOf("classmate@university.edu") }
        var date by remember { mutableStateOf("Friday, July 31") }
        var time by remember { mutableStateOf("2:00 PM PST") }

        Dialog(onDismissRequest = { showFriendDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Invite Peer for Friend Practice",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Practice Topic") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = friendEmail,
                        onValueChange = { friendEmail = it },
                        label = { Text("Friend's Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showFriendDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val scheduledInterview = InterviewEntity(
                                    id = "frd-${UUID.randomUUID()}",
                                    companyName = "Peer Practice",
                                    jobTitle = topic,
                                    status = "Upcoming",
                                    date = date,
                                    time = time,
                                    location = "Custom WebRTC Link",
                                    interviewer = friendEmail,
                                    notes = "Peer mock prep session for $topic.",
                                    type = "FRIEND"
                                )
                                onSchedulePracticeInterview(scheduledInterview)
                                showFriendDialog = false
                                onShowToast("Friend invitation link copied & scheduled!")
                            },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Send Invitation")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateQuizDialog(
    availableQuestions: List<QuestionEntity>,
    onDismiss: () -> Unit,
    onCreate: (String, String, List<QuestionEntity>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateMapOf<String, Boolean>() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Create Custom Quiz",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Quiz Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Select Questions:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                availableQuestions.forEach { q ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedIds[q.id] = !(selectedIds[q.id] ?: false) }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = selectedIds[q.id] ?: false,
                            onCheckedChange = { selectedIds[q.id] = it }
                        )
                        Text(text = q.questionText, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            val selectedList = availableQuestions.filter { selectedIds[it.id] == true }
                            if (title.isNotBlank() && selectedList.isNotEmpty()) {
                                onCreate(title, desc, selectedList)
                            }
                        }
                    ) { Text("Create") }
                }
            }
        }
    }
}

@Composable
private fun AIMockPracticeSection(
    questions: List<QuestionEntity>,
    onAnalyzeAnswerWithAI: suspend (String, String) -> MockAnswerAnalysis,
    onShowToast: (String) -> Unit
) {
    var selectedQuestionText by remember {
        mutableStateOf(
            if (questions.isNotEmpty()) questions.first().questionText
            else "Tell me about a time you handled a complex technical bug under deadline pressure."
        )
    }
    var userAnswerText by remember {
        mutableStateOf(
            "In my previous mobile project, we noticed recomposition lagging during list scrolling. I profiled the layout using Compose Tracing, optimized state calculations with derivedStateOf, and stabilized list item keys. As a result, scroll performance improved to 60fps."
        )
    }
    var isRecording by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var mockAnalysis by remember { mutableStateOf<MockAnswerAnalysis?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🤖 AI STAR Mock Interviewer",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                Text(
                    text = "Type or dictate your response. Gemini AI will evaluate your Situation, Task, Action, and Result (STAR) performance.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Select Interview Scenario:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Question Picker
        var dropdownExpanded by remember { mutableStateOf(false) }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { dropdownExpanded = true },
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedQuestionText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }
        }

        DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false }
        ) {
            questions.forEach { q ->
                DropdownMenuItem(
                    text = { Text(q.questionText) },
                    onClick = {
                        selectedQuestionText = q.questionText
                        dropdownExpanded = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Answer Input Field
        OutlinedTextField(
            value = userAnswerText,
            onValueChange = { userAnswerText = it },
            label = { Text("Your Interview Response") },
            placeholder = { Text("Describe the Situation, Task, Action you took, and quantifiable Results...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .testTag("user_answer_input"),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice Dictation Simulation Button
            IconButton(
                onClick = {
                    isRecording = !isRecording
                    if (isRecording) {
                        onShowToast("Voice Recording active... SPEAK NOW")
                    } else {
                        onShowToast("Voice Transcript saved!")
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isRecording) Color(0xFFEF4444) else MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("voice_record_button")
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Dictate Answer",
                    tint = if (isRecording) Color.White else MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = {
                    if (userAnswerText.isBlank()) {
                        onShowToast("Please enter an answer to evaluate.")
                        return@Button
                    }
                    isAnalyzing = true
                    coroutineScope.launch {
                        val result = onAnalyzeAnswerWithAI(selectedQuestionText, userAnswerText)
                        mockAnalysis = result
                        isAnalyzing = false
                        onShowToast("STAR Evaluation complete!")
                    }
                },
                enabled = !isAnalyzing,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("evaluate_answer_button")
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyzing with AI...")
                } else {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Evaluate with Gemini AI", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Analysis Report
        mockAnalysis?.let { analysis ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("star_analysis_report"),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STAR Evaluation Report",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = when {
                                analysis.starScore >= 85 -> Color(0xFFDCFCE7)
                                analysis.starScore >= 70 -> Color(0xFFFEF3C7)
                                else -> Color(0xFFFEE2E2)
                            }
                        ) {
                            Text(
                                text = "Score: ${analysis.starScore}/100",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        analysis.starScore >= 85 -> Color(0xFF15803D)
                                        analysis.starScore >= 70 -> Color(0xFFB45309)
                                        else -> Color(0xFFB91C1C)
                                    }
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Detailed STAR Breakdown:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))

                    FeedbackRow("S / T (Situation & Task)", analysis.situationFeedback)
                    FeedbackRow("Action Clarity", analysis.actionFeedback)
                    FeedbackRow("Measurable Result", analysis.resultFeedback)

                    Spacer(modifier = Modifier.height(12.dp))

                    if (analysis.strengths.isNotEmpty()) {
                        Text("Top Strengths:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF059669)))
                        analysis.strengths.forEach { str ->
                            Text("• $str", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (analysis.improvements.isNotEmpty()) {
                        Text("Areas for Improvement:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFD97706)))
                        analysis.improvements.forEach { imp ->
                            Text("• $imp", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Polished STAR Example Response:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = analysis.polishedResponse,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun FeedbackRow(title: String, desc: String) {
    if (desc.isNotBlank()) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
            Text(text = desc, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface))
        }
    }
}

@Composable
private fun QuizHistoryAndChallengesSection(
    recentQuizResults: List<QuizResult>,
    challenges: List<QuizChallengeEntity>,
    attempts: List<QuizAttemptEntity>,
    onStartChallenge: (QuizChallengeEntity) -> Unit,
    onShowToast: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. HISTORICAL QUIZ RESULTS SECTION
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Quiz Attempt History",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (recentQuizResults.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No completed quiz history yet.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Complete practice quizzes or challenges to view your attempt scores & review answers here.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            RecentQuizzesSection(
                recentQuizzes = recentQuizResults,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // 2. MULTIPLAYER CHALLENGES & LEADERBOARDS
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Multiplayer Quiz Challenges 🏆",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        QuizChallengesSection(
            challenges = challenges,
            attempts = attempts,
            onStartChallenge = onStartChallenge,
            onShowToast = onShowToast
        )
    }
}

@Composable
private fun QuizChallengesSection(
    challenges: List<QuizChallengeEntity>,
    attempts: List<QuizAttemptEntity>,
    onStartChallenge: (QuizChallengeEntity) -> Unit,
    onShowToast: (String) -> Unit
) {
    var codeInput by remember { mutableStateOf("") }
    var selectedLeaderboardChallenge by remember { mutableStateOf<QuizChallengeEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        // CODE SEARCH / DEEP LINK ENTRY CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enter Quiz Challenge Code",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Access challenge via code or deep-link /quiz-challenge/[code]",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it.uppercase().take(6) },
                        placeholder = { Text("e.g. AB3D9X") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("challenge_code_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val code = codeInput.trim().uppercase()
                            val found = challenges.find { it.code.equals(code, ignoreCase = true) }
                            if (found != null) {
                                onStartChallenge(found)
                            } else if (code.length == 6) {
                                onShowToast("Searching challenge $code... Starting mock challenge session.")
                                val mockChal = QuizChallengeEntity(
                                    id = "chal-$code",
                                    code = code,
                                    creatorName = "Peer Challenger",
                                    quizTitle = "Multiplayer Challenge $code",
                                    quizDescription = "Special competition questions for $code",
                                    questions = listOf(
                                        QuestionEntity("c1", "System Design: How to design scalable rate limiting?", "System Design", "Hard", "All algorithms have trade-offs.", listOf("Token Bucket", "Fixed Window", "Leaky Bucket", "All of the above"), 3),
                                        QuestionEntity("c2", "Behavioral: Describe a conflict resolution experience.", "Behavioral", "Medium", "Active listening aligns goals.", listOf("Avoid it", "Listen actively & align goals", "Escalate immediately", "Ignore"), 1)
                                    ),
                                    creatorScorePercentage = 85,
                                    creatorCorrectAnswers = 2,
                                    creatorTotalQuestions = 2,
                                    creatorElapsedTimeSeconds = 120,
                                    invitedFriends = listOf("You", "Alex")
                                )
                                onStartChallenge(mockChal)
                            } else {
                                onShowToast("Please enter a valid 6-character code!")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("submit_challenge_code_button")
                    ) {
                        Text("Join")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "ACTIVE MULTIPLAYER CHALLENGES (${challenges.size})",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        challenges.forEach { challenge ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "CODE: ${challenge.code}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "Creator: ${challenge.creatorName}",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = challenge.quizTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${challenge.questions.size} Questions • Creator Score: ${challenge.creatorScorePercentage}% (${challenge.creatorElapsedTimeSeconds}s)",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onStartChallenge(challenge) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("accept_challenge_button_${challenge.code}"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Take Challenge")
                        }

                        OutlinedButton(
                            onClick = { selectedLeaderboardChallenge = challenge },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Leaderboard")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // LEADERBOARD DIALOG
    if (selectedLeaderboardChallenge != null) {
        val chal = selectedLeaderboardChallenge!!
        val chalAttempts = attempts.filter { it.challengeCode.equals(chal.code, ignoreCase = true) }

        AlertDialog(
            onDismissRequest = { selectedLeaderboardChallenge = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Leaderboard: ${chal.code}")
                }
            },
            text = {
                Column {
                    Text(
                        text = chal.quizTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LeaderboardRowItem(
                        rank = 1,
                        name = "${chal.creatorName} (Creator)",
                        score = chal.creatorScorePercentage,
                        timeSec = chal.creatorElapsedTimeSeconds,
                        isUser = chal.creatorName == "Alex Rivera"
                    )

                    chalAttempts.forEachIndexed { idx, att ->
                        LeaderboardRowItem(
                            rank = idx + 2,
                            name = att.userName,
                            score = att.scorePercentage,
                            timeSec = att.elapsedTimeSeconds,
                            isUser = att.userName == "Alex Rivera"
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedLeaderboardChallenge = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun LeaderboardRowItem(
    rank: Int,
    name: String,
    score: Int,
    timeSec: Int,
    isUser: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (isUser) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (rank) {
                        1 -> "🥇 #1"
                        2 -> "🥈 #2"
                        3 -> "🥉 #3"
                        else -> "#$rank"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = name,
                    fontWeight = if (isUser) FontWeight.Bold else FontWeight.Normal,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "$score% (${timeSec}s)",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

