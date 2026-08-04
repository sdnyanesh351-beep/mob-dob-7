package com.example.ui.screens

import com.example.ui.components.ShareMilestoneDialog
import com.example.ui.components.ShareType

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QuestionEntity
import com.example.data.QuizEntity
import com.example.data.QuizResult

@Composable
fun PreQuizConfigDialog(
    quiz: QuizEntity,
    onDismiss: () -> Unit,
    onStartQuiz: (isChallengeMode: Boolean, isFeedbackEnabled: Boolean, isInstantFeedbackEnabled: Boolean, timerMinutes: Int) -> Unit
) {
    var isChallengeMode by remember { mutableStateOf(false) }
    var isFeedbackEnabled by remember { mutableStateOf(true) }
    var isInstantFeedbackEnabled by remember { mutableStateOf(false) }
    var selectedTimerMinutes by remember { mutableIntStateOf(10) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Quiz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Configure Quiz",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = quiz.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "${quiz.questions.size} Questions • Choose quiz mode and feedback settings before starting.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SELECT QUIZ MODE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Normal Practice Mode Option Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isChallengeMode = false }
                        .testTag("select_normal_mode"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (!isChallengeMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (!isChallengeMode) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !isChallengeMode,
                            onClick = { isChallengeMode = false }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Standard Practice Mode",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Self-paced practice with instant answer explanations & bookmarks",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Challenge Mode Option Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isChallengeMode = true }
                        .testTag("select_challenge_mode"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isChallengeMode) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (isChallengeMode) BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary) else null
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isChallengeMode,
                            onClick = { isChallengeMode = true },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.tertiary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Multiplayer Challenge 🏆",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChallengeMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "Competitive mode! Generates a 6-digit code for friends to join your leaderboard",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "QUIZ TIMER SETTING",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Timer Duration Selector
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Countdown Timer",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Text(
                                text = if (selectedTimerMinutes > 0) "${selectedTimerMinutes} Mins" else "Untimed",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val timerOptions = listOf(5 to "5m", 10 to "10m", 15 to "15m", 0 to "Untimed")
                            timerOptions.forEach { (mins, label) ->
                                val isSelected = selectedTimerMinutes == mins
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedTimerMinutes = mins }
                                        .testTag("timer_option_$mins"),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(10.dp),
                                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rating & Feedback Toggle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "5-Star Ratings & Comments",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Allow rating individual questions & submitting custom feedback comments",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Switch(
                            checked = isFeedbackEnabled,
                            onCheckedChange = { isFeedbackEnabled = it },
                            modifier = Modifier.testTag("toggle_prequiz_feedback")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onStartQuiz(isChallengeMode, isFeedbackEnabled, isInstantFeedbackEnabled, selectedTimerMinutes) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("start_quiz_confirm_button")
            ) {
                Text(if (isChallengeMode) "Start Challenge 🏆" else "Start Practice")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_quiz_config_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PaletteLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = color,
            modifier = Modifier.size(10.dp)
        ) {}
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizPlayerView(
    quiz: QuizEntity,
    isChallengeMode: Boolean = false,
    isFeedbackEnabled: Boolean = true,
    initialInstantFeedbackEnabled: Boolean = false,
    initialTimerMinutes: Int = 10,
    onFinishQuiz: (QuizResult) -> Unit,
    onExit: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<String, Int>() }
    val markedForReview = remember { mutableStateMapOf<String, Boolean>() }
    val bookmarkedQuestions = remember { mutableStateMapOf<String, Boolean>() }

    // Question Feedback & Rating States
    val questionRatings = remember { mutableStateMapOf<String, Int>() }
    val questionFeedbacks = remember { mutableStateMapOf<String, String>() }

    val buildQuizResult: () -> QuizResult = {
        var correctCount = 0
        quiz.questions.forEach { q ->
            if (userAnswers[q.id] == q.correctOptionIndex) {
                correctCount++
            }
        }
        val scorePct = if (quiz.questions.isNotEmpty()) {
            ((correctCount.toFloat() / quiz.questions.size) * 100).toInt()
        } else 0

        QuizResult(
            quizTitle = quiz.title,
            totalQuestions = quiz.questions.size,
            correctAnswers = correctCount,
            scorePercentage = scorePct,
            userAnswers = userAnswers.toMap(),
            quiz = quiz,
            durationSeconds = 115,
            isChallengeMode = isChallengeMode,
            markedForReviewIds = markedForReview.filterValues { it }.keys,
            bookmarkedIds = bookmarkedQuestions.filterValues { it }.keys,
            questionRatings = questionRatings.toMap(),
            questionFeedbacks = questionFeedbacks.toMap()
        )
    }

    // Countdown Timer State
    var remainingSeconds by remember(quiz.id, initialTimerMinutes) { mutableIntStateOf(initialTimerMinutes * 60) }

    if (initialTimerMinutes > 0) {
        LaunchedEffect(remainingSeconds) {
            if (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds -= 1
            } else {
                onFinishQuiz(buildQuizResult())
            }
        }
    }

    var isInstantFeedbackEnabled by remember(initialInstantFeedbackEnabled) { mutableStateOf(initialInstantFeedbackEnabled) }
    var isFullscreen by remember { mutableStateOf(false) }
    var showQuestionSheet by remember { mutableStateOf(false) }
    var showPaletteDrawer by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var isCommentsExpanded by remember { mutableStateOf(false) }

    // Per-question comments map: questionId -> list of comments
    val questionComments = remember {
        mutableStateMapOf<String, MutableList<String>>().apply {
            quiz.questions.forEach { q ->
                put(q.id, mutableListOf("Great question for technical rounds!", "Remember to check edge cases here."))
            }
        }
    }

    val currentQuestion = quiz.questions.getOrNull(currentIndex) ?: return
    val progress = (currentIndex + 1).toFloat() / quiz.questions.size

    // Initialize bookmarks from QuestionEntity
    quiz.questions.forEach { q ->
        if (q.isBookmarked && !bookmarkedQuestions.containsKey(q.id)) {
            bookmarkedQuestions[q.id] = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("active_quiz_player")
        ) {
            // 1. HEADER & CONTROLS BAR
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Exit, Title, Topic & Challenge Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onExit,
                                modifier = Modifier.testTag("exit_quiz_button")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Exit Quiz")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = quiz.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        maxLines = 1
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Difficulty Badge
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (currentQuestion.difficulty.lowercase()) {
                                            "easy" -> Color(0xFFDCFCE7)
                                            "hard" -> Color(0xFFFEE2E2)
                                            else -> Color(0xFFFEF3C7)
                                        }
                                    ) {
                                        Text(
                                            text = currentQuestion.difficulty,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = when (currentQuestion.difficulty.lowercase()) {
                                                    "easy" -> Color(0xFF166534)
                                                    "hard" -> Color(0xFF991B1B)
                                                    else -> Color(0xFF92400E)
                                                },
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    // Challenge Mode Trophy Badge
                                    if (isChallengeMode) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFFFF7ED),
                                            border = BorderStroke(1.dp, Color(0xFFFDBA74))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.EmojiEvents,
                                                    contentDescription = null,
                                                    tint = Color(0xFFEA580C),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "Challenge Mode",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFC2410C),
                                                        fontSize = 10.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Right: Countdown Timer, Fullscreen & Question Counter
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Countdown Timer Chip
                            if (initialTimerMinutes > 0) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (remainingSeconds < 60) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.primaryContainer,
                                    border = if (remainingSeconds < 60) BorderStroke(1.dp, Color(0xFFEF4444)) else null,
                                    modifier = Modifier.testTag("quiz_countdown_timer_chip")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = "Countdown Timer",
                                            tint = if (remainingSeconds < 60) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        val mins = remainingSeconds / 60
                                        val secs = remainingSeconds % 60
                                        Text(
                                            text = String.format("%02d:%02d", mins, secs),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (remainingSeconds < 60) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            // Fullscreen Mode Toggle
                            IconButton(
                                onClick = { isFullscreen = !isFullscreen },
                                modifier = Modifier.testTag("fullscreen_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${currentIndex + 1} / ${quiz.questions.size}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Visual Progress Bar
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // 2. MAIN SCROLLABLE QUESTION CONTENT
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {


                // QUESTION CARD & OPTIONS LIST
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Category & ID Tag Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = currentQuestion.category,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Text(
                                text = "#Q-${currentQuestion.id.uppercase()}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Question Text
                        Text(
                            text = currentQuestion.questionText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Options List (A, B, C, D)
                        currentQuestion.options.forEachIndexed { optIndex, optionText ->
                            val selectedOpt = userAnswers[currentQuestion.id]
                            val isSelected = selectedOpt == optIndex
                            val isCorrect = optIndex == currentQuestion.correctOptionIndex

                            // Instant feedback colors
                            val optionBgColor = when {
                                isInstantFeedbackEnabled && selectedOpt != null -> {
                                    when {
                                        isCorrect -> Color(0xFFDCFCE7) // Green
                                        isSelected && !isCorrect -> Color(0xFFFEE2E2) // Red
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                }
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            val optionTextColor = when {
                                isInstantFeedbackEnabled && selectedOpt != null -> {
                                    when {
                                        isCorrect -> Color(0xFF14532D) // Dark Green text for high contrast on light green
                                        isSelected && !isCorrect -> Color(0xFF7F1D1D) // Dark Red text for high contrast on light red
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                }
                                isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            val badgeBgColor = when {
                                isInstantFeedbackEnabled && selectedOpt != null -> {
                                    when {
                                        isCorrect -> Color(0xFF166534)
                                        isSelected && !isCorrect -> Color(0xFF991B1B)
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                }
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.surface
                            }

                            val badgeTextColor = when {
                                isInstantFeedbackEnabled && selectedOpt != null -> {
                                    when {
                                        isCorrect || (isSelected && !isCorrect) -> Color.White
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                }
                                isSelected -> Color.White
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            val optionBorder = when {
                                isInstantFeedbackEnabled && selectedOpt != null -> {
                                    when {
                                        isCorrect -> BorderStroke(1.5.dp, Color(0xFF166534))
                                        isSelected && !isCorrect -> BorderStroke(1.5.dp, Color(0xFF991B1B))
                                        else -> null
                                    }
                                }
                                isSelected -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                else -> null
                            }

                            val optionLetter = ('A' + optIndex).toString()

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { userAnswers[currentQuestion.id] = optIndex }
                                    .testTag("quiz_option_${currentIndex}_$optIndex"),
                                color = optionBgColor,
                                shape = RoundedCornerShape(14.dp),
                                border = optionBorder
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Choice Option Letter Badge (A, B, C, D)
                                    Surface(
                                        shape = CircleShape,
                                        color = badgeBgColor,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = optionLetter,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = badgeTextColor
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = optionText,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = optionTextColor
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Instant Feedback Status Icon
                                    if (isInstantFeedbackEnabled && selectedOpt != null) {
                                        if (isCorrect) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Correct",
                                                tint = Color(0xFF166534),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Cancel,
                                                contentDescription = "Incorrect",
                                                tint = Color(0xFF991B1B),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Explanatory Feedback Box in Instant Feedback Mode
                        if (isInstantFeedbackEnabled && userAnswers[currentQuestion.id] != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Explanatory Answer Feedback",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentQuestion.sampleAnswer,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 5-STAR RATING & QUESTION FEEDBACK COMMENT CARD (WHEN ENABLED IN PRE-QUIZ SETTINGS)
                if (isFeedbackEnabled) {
                    var currentRating by remember(currentQuestion.id) { mutableIntStateOf(questionRatings[currentQuestion.id] ?: 0) }
                    var feedbackText by remember(currentQuestion.id) { mutableStateOf(questionFeedbacks[currentQuestion.id] ?: "") }
                    var feedbackSaved by remember(currentQuestion.id) { mutableStateOf(false) }
                    var isRateFeedbackExpanded by remember(currentQuestion.id) { mutableStateOf(false) }

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Expandable Header Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isRateFeedbackExpanded = !isRateFeedbackExpanded }
                                    .testTag("toggle_rate_feedback_header"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.RateReview,
                                        contentDescription = "Rate & Feedback for Question",
                                        tint = Color(0xFFEAB308),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    if (currentRating > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFFEF3C7)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "$currentRating",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFB45309)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (feedbackSaved) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFDCFCE7)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Saved",
                                                tint = Color(0xFF166534),
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .padding(1.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Icon(
                                        imageVector = if (isRateFeedbackExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle Rate and Feedback",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = isRateFeedbackExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    // Interactive 5 Star Rating Bar
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        (1..5).forEach { star ->
                                            IconButton(
                                                onClick = {
                                                    currentRating = star
                                                    questionRatings[currentQuestion.id] = star
                                                    feedbackSaved = true
                                                },
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .testTag("star_rating_${currentQuestion.id}_$star")
                                            ) {
                                                Icon(
                                                    imageVector = if (star <= currentRating) Icons.Default.Star else Icons.Default.StarBorder,
                                                    contentDescription = "Star $star",
                                                    tint = if (star <= currentRating) Color(0xFFEAB308) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Feedback Comment Field
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = feedbackText,
                                            onValueChange = {
                                                feedbackText = it
                                                questionFeedbacks[currentQuestion.id] = it
                                            },
                                            placeholder = { Text("Comment on difficulty, wording, or content...", fontSize = 12.sp) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("question_feedback_input_${currentQuestion.id}"),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = {
                                                questionFeedbacks[currentQuestion.id] = feedbackText
                                                feedbackSaved = true
                                            },
                                            modifier = Modifier.testTag("submit_question_feedback_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Send,
                                                contentDescription = "Save Feedback",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // BOTTOM ACTIONS & REVISIT CONTROLS
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mark for Review (Icon only)
                            val isMarked = markedForReview[currentQuestion.id] == true
                            IconButton(
                                onClick = { markedForReview[currentQuestion.id] = !isMarked },
                                modifier = Modifier.testTag("mark_for_review_button")
                            ) {
                                Icon(
                                    imageVector = if (isMarked) Icons.Default.Flag else Icons.Default.OutlinedFlag,
                                    contentDescription = if (isMarked) "Marked for Review" else "Mark for Review",
                                    tint = if (isMarked) Color(0xFFEA580C) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Bookmark Question (Icon only)
                            val isStarred = bookmarkedQuestions[currentQuestion.id] == true
                            IconButton(
                                onClick = { bookmarkedQuestions[currentQuestion.id] = !isStarred },
                                modifier = Modifier.testTag("bookmark_question_button")
                            ) {
                                Icon(
                                    imageVector = if (isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = if (isStarred) "Starred" else "Bookmark Question",
                                    tint = if (isStarred) Color(0xFFEAB308) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Report / Flag Error Modal Button (Icon only)
                            IconButton(
                                onClick = { showReportDialog = true },
                                modifier = Modifier.testTag("flag_error_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReportProblem,
                                    contentDescription = "Report Issue",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Question Comments Discussion Section (Icon + Badge)
                            IconButton(
                                onClick = { isCommentsExpanded = !isCommentsExpanded },
                                modifier = Modifier.testTag("toggle_comments_button")
                            ) {
                                val commentCount = questionComments[currentQuestion.id]?.size ?: 0
                                Box(contentAlignment = Alignment.TopEnd) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Comment,
                                        contentDescription = "Question Discussion Thread",
                                        tint = if (isCommentsExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    if (commentCount > 0) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.offset(x = 6.dp, y = (-4).dp)
                                        ) {
                                            Text(
                                                text = "$commentCount",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 9.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = isCommentsExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                val commentsList = questionComments[currentQuestion.id] ?: mutableListOf()
                                commentsList.forEach { commentStr ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = commentStr,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                var newCommentText by remember(currentQuestion.id) { mutableStateOf("") }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newCommentText,
                                        onValueChange = { newCommentText = it },
                                        placeholder = { Text("Post a comment...", fontSize = 12.sp) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            if (newCommentText.isNotBlank()) {
                                                questionComments[currentQuestion.id]?.add("You: ${newCommentText.trim()}")
                                                newCommentText = ""
                                            }
                                        },
                                        modifier = Modifier.testTag("post_comment_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Send",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. STICKY BOTTOM NAVIGATION BAR
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (currentIndex > 0) currentIndex-- },
                        enabled = currentIndex > 0,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("prev_question_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }

                    // Question palette drawer toggle button
                    IconButton(
                        onClick = { showPaletteDrawer = true },
                        modifier = Modifier.testTag("open_question_palette_sticky_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Question Grid Palette",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (currentIndex < quiz.questions.size - 1) {
                        Button(
                            onClick = { currentIndex++ },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("next_question_button")
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null)
                        }
                    } else {
                        Button(
                            onClick = {
                                onFinishQuiz(buildQuizResult())
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("submit_quiz_button")
                        ) {
                            Text("Submit Quiz")
                        }
                    }
                }
            }
        }

        // 3. RIGHT-SIDE QUESTION PALETTE DRAWER (SLIDING OVERLAY) & EXPAND HANDLE
        if (!showPaletteDrawer) {
            Surface(
                shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 8.dp,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .clickable { showPaletteDrawer = true }
                    .testTag("toggle_palette_drawer_button")
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                        contentDescription = "Expand Palette",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showPaletteDrawer,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .shadow(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Palette Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GridOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Question Palette",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // Collapse Palette > Button
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { showPaletteDrawer = false }
                                .testTag("close_palette_drawer")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Collapse",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                                    contentDescription = "Collapse Palette",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Legend
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "STATUS LEGEND",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                PaletteLegendItem(color = MaterialTheme.colorScheme.primary, label = "Answered")
                                PaletteLegendItem(color = Color(0xFFEA580C), label = "Review")
                                PaletteLegendItem(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), label = "Unanswered")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Grid of question chips
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        itemsIndexed(quiz.questions) { index, q ->
                            val isAnswered = userAnswers.containsKey(q.id)
                            val isMarked = markedForReview[q.id] == true
                            val isStarred = bookmarkedQuestions[q.id] == true
                            val isCurrent = index == currentIndex

                            val chipBgColor = when {
                                isMarked -> Color(0xFFFED7AA)
                                isAnswered -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            val chipTextColor = when {
                                isMarked -> Color(0xFFC2410C)
                                isAnswered -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Surface(
                                shape = CircleShape,
                                color = chipBgColor,
                                border = if (isCurrent) BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        currentIndex = index
                                    }
                                    .testTag("palette_chip_$index")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = chipTextColor
                                        )
                                    )

                                    if (isStarred) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Starred",
                                            tint = Color(0xFFEAB308),
                                            modifier = Modifier
                                                .size(12.dp)
                                                .align(Alignment.TopEnd)
                                                .padding(top = 4.dp, end = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            showPaletteDrawer = false
                            onFinishQuiz(buildQuizResult())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("palette_submit_quiz_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Quiz", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = { showPaletteDrawer = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("close_palette_drawer_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close Palette")
                    }
                }
            }
        }
    }

    // 3. SIDEBAR QUESTION LIST SHEET
    if (showQuestionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQuestionSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Quiz Questions Overview",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "Jump directly to any question or review marked flags",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.height(320.dp)) {
                    itemsIndexed(quiz.questions) { index, q ->
                        val isAnswered = userAnswers.containsKey(q.id)
                        val isMarked = markedForReview[q.id] == true
                        val isStarred = bookmarkedQuestions[q.id] == true
                        val isCurrent = index == currentIndex

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    currentIndex = index
                                    showQuestionSheet = false
                                },
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = if (isCurrent) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isAnswered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (isAnswered) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = q.questionText,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        maxLines = 1,
                                        modifier = Modifier.width(200.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isAnswered) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Answered",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    if (isMarked) {
                                        Icon(
                                            imageVector = Icons.Default.Flag,
                                            contentDescription = "Marked",
                                            tint = Color(0xFFEA580C),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    if (isStarred) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Bookmarked",
                                            tint = Color(0xFFEAB308),
                                            modifier = Modifier.size(16.dp)
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

    // 4. REPORT / FLAG QUESTION ERROR MODAL
    var showReportSubmittedToast by remember { mutableStateOf(false) }

    if (showReportSubmittedToast) {
        AlertDialog(
            onDismissRequest = { showReportSubmittedToast = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF166534),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { Text("Issue Reported", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Text(
                    text = "Thank you for reporting this issue on Question #${currentQuestion.id.uppercase()}. Our content team will review it shortly.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { showReportSubmittedToast = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("OK")
                }
            },
            modifier = Modifier.testTag("report_submitted_dialog")
        )
    }

    if (showReportDialog) {
        var reportType by remember { mutableStateOf("Typo / Formatting Error") }
        var reportDetails by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.ReportProblem,
                    contentDescription = "Report Issue",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Report Issue in Question #${currentQuestion.id.uppercase()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Question Preview Snippet
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Category: ${currentQuestion.category}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentQuestion.questionText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 2
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Select the issue type:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val issueTypes = listOf(
                        "Typo / Formatting Error",
                        "Incorrect Answer Choice",
                        "Unclear / Misleading Question",
                        "Incorrect Explanation",
                        "Other Issue"
                    )
                    issueTypes.forEachIndexed { idx, type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { reportType = type }
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                .testTag("report_type_option_$idx")
                        ) {
                            RadioButton(
                                selected = reportType == type,
                                onClick = { reportType = type }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = type,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (reportType == type) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = reportDetails,
                        onValueChange = { reportDetails = it },
                        label = { Text("Additional Details (optional)") },
                        placeholder = { Text("Describe the error or suggested correction...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("report_issue_details_input"),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReportDialog = false
                        showReportSubmittedToast = true
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("report_issue_submit_button")
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showReportDialog = false },
                    modifier = Modifier.testTag("report_issue_cancel_button")
                ) {
                    Text("Cancel")
                }
            },
            modifier = Modifier.testTag("report_issue_dialog")
        )
    }
}

// ====================================================================
// COMPREHENSIVE RESULTS DASHBOARD
// ====================================================================

@Composable
fun QuizResultView(
    result: QuizResult,
    onDismiss: () -> Unit,
    onCreateChallenge: ((QuizResult, String, List<String>) -> com.example.data.QuizChallengeEntity)? = null,
    onShareToCommunity: ((String) -> Unit)? = null,
    onShowToast: ((String) -> Unit)? = null
) {
    var isReviewModeActive by remember { mutableStateOf(false) }
    var showPdfDialog by remember { mutableStateOf(false) }
    var showCreateChallengeDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var createdChallenge by remember { mutableStateOf<com.example.data.QuizChallengeEntity?>(null) }

    val quiz = result.quiz

    if (isReviewModeActive && quiz != null) {
        ReviewModeView(
            quiz = quiz,
            userAnswers = result.userAnswers,
            onBackToDashboard = { isReviewModeActive = false }
        )
        return
    }

    if (showShareDialog) {
        ShareMilestoneDialog(
            shareType = ShareType.QUIZ_SCORE,
            title = "Share Quiz Score",
            subtitle = "Showcase your ${result.scorePercentage}% score to peers and social platforms!",
            defaultMessage = "🎯 I scored ${result.scorePercentage}% (${result.correctAnswers}/${result.totalQuestions}) on '${result.quizTitle}' in JobTraq! 🚀 #JobTraq #InterviewPrep",
            scorePercentage = result.scorePercentage,
            quizTitle = result.quizTitle,
            onShareToCommunity = { text ->
                onShareToCommunity?.invoke(text)
            },
            onDismiss = { showShareDialog = false },
            onShowToast = { msg -> onShowToast?.invoke(msg) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("quiz_results_dashboard")
    ) {
        // TOP SCORE BANNER
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Quiz Completed! 🎉",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = result.quizTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${result.scorePercentage}%",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "Overall Accuracy",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${result.correctAnswers} / ${result.totalQuestions}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Text(
                            text = "Correct Answers",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${result.durationSeconds}s",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Text(
                            text = "Completion Time",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // MULTIPLAYER QUIZ CHALLENGE BANNER
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
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
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Challenge",
                                tint = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (createdChallenge != null) "Challenge Live: ${createdChallenge?.code}" else "Convert to Multiplayer Challenge",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (createdChallenge != null) "Code ${createdChallenge?.code} active! Friends invited." else "Generate 6-char code (e.g. AB3D9X) to challenge friends",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (createdChallenge == null && onCreateChallenge != null) {
                            createdChallenge = onCreateChallenge(result, "Alex Rivera", listOf("Jordan Smith", "Taylor Chen", "Sam Morgan"))
                        }
                        showCreateChallengeDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("convert_to_challenge_button")
                ) {
                    Text(
                        text = if (createdChallenge != null) "View Code" else "Challenge Friends",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ACTION BAR (PDF Download, Review Mode, Share Score)
        Button(
            onClick = { showShareDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("share_quiz_score_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Share, contentDescription = "Share Score", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share Score to Community & Social Platforms", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { isReviewModeActive = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("review_questions_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Review Questions")
            }

            OutlinedButton(
                onClick = { showPdfDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("download_pdf_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Download PDF")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // INTERACTIVE DATA VISUALIZATIONS (PIE & BAR CHARTS)
        Text(
            text = "PERFORMANCE ANALYTICS",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Overall Response Breakdown",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))

                val incorrectCount = result.totalQuestions - result.correctAnswers
                val skippedCount = 0 // All answered in practice
                AccuracyPieChart(
                    correct = result.correctAnswers,
                    incorrect = incorrectCount,
                    skipped = skippedCount
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Category Accuracy Breakdown",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))

                CategoryAccuracyBarChart(
                    quiz = quiz,
                    userAnswers = result.userAnswers
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // TOPIC METRICS TABLE
        Text(
            text = "DETAILED TOPIC METRICS",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        MetricsTable(
            quiz = quiz,
            userAnswers = result.userAnswers
        )

        Spacer(modifier = Modifier.height(20.dp))

        // CHALLENGE LEADERBOARD
        if (result.isChallengeMode) {
            Text(
                text = "CHALLENGE LEADERBOARD",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            ChallengeLeaderboardView(userScorePct = result.scorePercentage, userTimeSec = result.durationSeconds)

            Spacer(modifier = Modifier.height(20.dp))
        }

        // DISMISS BUTTON
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("back_to_quizzes_button"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Back to Practice Quizzes")
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // PDF REPORT DOWNLOAD DIALOG
    if (showPdfDialog) {
        AlertDialog(
            onDismissRequest = { showPdfDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFDC2626))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PDF Report Generated")
                }
            },
            text = {
                Column {
                    Text("Your complete quiz performance report has been compiled successfully.")
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "📄 ${result.quizTitle}_Report.pdf", fontWeight = FontWeight.Bold)
                            Text(text = "Includes: Question list, user selections, correct option keys, and detailed sample explanations.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPdfDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save to Downloads")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPdfDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // MULTIPLAYER CHALLENGE CREATED DIALOG
    if (showCreateChallengeDialog && createdChallenge != null) {
        val challenge = createdChallenge!!
        AlertDialog(
            onDismissRequest = { showCreateChallengeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Multiplayer Challenge Created!")
                }
            },
            text = {
                Column {
                    Text(
                        text = "Your completed quiz session has been converted into a unique 6-character competition!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "UNIQUE CHALLENGE CODE",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onTertiaryContainer)
                            )
                            Text(
                                text = challenge.code,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    letterSpacing = 4.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Direct URL: /quiz-challenge/${challenge.code}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "🔥 Challenge invites fired to: ${challenge.invitedFriends.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showCreateChallengeDialog = false }) {
                    Text("Got It!")
                }
            }
        )
    }
}

// ====================================================================
// CHARTS & GRAPHICAL COMPONENTS
// ====================================================================

@Composable
fun AccuracyPieChart(
    correct: Int,
    incorrect: Int,
    skipped: Int
) {
    val total = (correct + incorrect + skipped).coerceAtLeast(1)
    val correctAngle = (correct.toFloat() / total) * 360f
    val incorrectAngle = (incorrect.toFloat() / total) * 360f
    val skippedAngle = (skipped.toFloat() / total) * 360f

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f

                // Draw Correct Arc (Green)
                if (correctAngle > 0f) {
                    drawArc(
                        color = Color(0xFF22C55E),
                        startAngle = startAngle,
                        sweepAngle = correctAngle,
                        useCenter = false,
                        style = Stroke(width = 24f, cap = StrokeCap.Round)
                    )
                    startAngle += correctAngle
                }

                // Draw Incorrect Arc (Red)
                if (incorrectAngle > 0f) {
                    drawArc(
                        color = Color(0xFFEF4444),
                        startAngle = startAngle,
                        sweepAngle = incorrectAngle,
                        useCenter = false,
                        style = Stroke(width = 24f, cap = StrokeCap.Round)
                    )
                    startAngle += incorrectAngle
                }

                // Draw Skipped Arc (Gray)
                if (skippedAngle > 0f) {
                    drawArc(
                        color = Color(0xFF9CA3AF),
                        startAngle = startAngle,
                        sweepAngle = skippedAngle,
                        useCenter = false,
                        style = Stroke(width = 24f, cap = StrokeCap.Round)
                    )
                }
            }

            val pct = ((correct.toFloat() / total) * 100).toInt()
            Text(
                text = "$pct%",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ChartLegendItem(color = Color(0xFF22C55E), label = "Correct", value = "$correct")
            ChartLegendItem(color = Color(0xFFEF4444), label = "Incorrect", value = "$incorrect")
            ChartLegendItem(color = Color(0xFF9CA3AF), label = "Skipped", value = "$skipped")
        }
    }
}

@Composable
private fun ChartLegendItem(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun CategoryAccuracyBarChart(
    quiz: QuizEntity?,
    userAnswers: Map<String, Int>
) {
    val categories = quiz?.questions?.map { it.category }?.distinct() ?: listOf("Technical", "System Design", "Behavioral")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categories.forEach { cat ->
            val catQuestions = quiz?.questions?.filter { it.category == cat } ?: emptyList()
            var catCorrect = 0
            catQuestions.forEach { q ->
                if (userAnswers[q.id] == q.correctOptionIndex) catCorrect++
            }
            val catPct = if (catQuestions.isNotEmpty()) ((catCorrect.toFloat() / catQuestions.size) * 100).toInt() else 80

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "$catPct% ($catCorrect/${catQuestions.size.coerceAtLeast(1)})",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { catPct / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = when {
                        catPct >= 80 -> Color(0xFF22C55E)
                        catPct >= 50 -> Color(0xFFEAB308)
                        else -> Color(0xFFEF4444)
                    }
                )
            }
        }
    }
}

// ====================================================================
// METRICS TABLE & LEADERBOARD
// ====================================================================

@Composable
fun MetricsTable(
    quiz: QuizEntity?,
    userAnswers: Map<String, Int>
) {
    val questions = quiz?.questions ?: emptyList()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Topic Tag", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1.5f))
                Text(text = "Category", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1.2f))
                Text(text = "Result", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(6.dp))

            questions.forEachIndexed { idx, q ->
                val isCorrect = userAnswers[q.id] == q.correctOptionIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Q${idx + 1}: #${q.id}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.weight(1.5f)
                    )
                    Text(
                        text = q.category,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.weight(1.2f)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isCorrect) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(
                                text = if (isCorrect) "PASSED" else "FAILED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) Color(0xFF166534) else Color(0xFF991B1B),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
                if (idx < questions.size - 1) HorizontalDivider()
            }
        }
    }
}

@Composable
fun ChallengeLeaderboardView(
    userScorePct: Int,
    userTimeSec: Int
) {
    val participants = listOf(
        Triple("Sarah Jenkins", 100, "1m 15s"),
        Triple("You (Player)", userScorePct, "${userTimeSec / 60}m ${userTimeSec % 60}s"),
        Triple("Alex Rivera", 85, "1m 50s"),
        Triple("David Kim", 75, "2m 10s")
    ).sortedByDescending { it.second }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Leaderboard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Live Multiplayer Rank Standings",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            participants.forEachIndexed { rank, (name, score, timeStr) ->
                val isUser = name.startsWith("You")
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = when (rank) {
                                    0 -> Color(0xFFEAB308) // Gold
                                    1 -> Color(0xFF94A3B8) // Silver
                                    2 -> Color(0xFFD97706) // Bronze
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "#${rank + 1}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (rank < 3) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isUser) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$score%",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// REVIEW MODE VIEW
// ====================================================================

@Composable
fun ReviewModeView(
    quiz: QuizEntity,
    userAnswers: Map<String, Int>,
    onBackToDashboard: () -> Unit
) {
    var reviewIndex by remember { mutableIntStateOf(0) }
    val currentQ = quiz.questions.getOrNull(reviewIndex) ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("review_mode_view")
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackToDashboard) {
                        Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Question Review Mode",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${reviewIndex + 1} of ${quiz.questions.size} Questions",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onBackToDashboard,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Exit Review")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = currentQ.questionText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    currentQ.options.forEachIndexed { optIndex, optionText ->
                        val userChosen = userAnswers[currentQ.id] == optIndex
                        val isCorrectKey = optIndex == currentQ.correctOptionIndex

                        val cardColor = when {
                            isCorrectKey -> Color(0xFFDCFCE7)
                            userChosen && !isCorrectKey -> Color(0xFFFEE2E2)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        val optionTextColor = when {
                            isCorrectKey -> Color(0xFF14532D)
                            userChosen && !isCorrectKey -> Color(0xFF7F1D1D)
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        val borderColor = when {
                            isCorrectKey -> BorderStroke(1.5.dp, Color(0xFF166534))
                            userChosen && !isCorrectKey -> BorderStroke(1.5.dp, Color(0xFF991B1B))
                            else -> null
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            color = cardColor,
                            shape = RoundedCornerShape(12.dp),
                            border = borderColor
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${('A' + optIndex)}. $optionText",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isCorrectKey || userChosen) FontWeight.Bold else FontWeight.Medium,
                                        color = optionTextColor
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                if (isCorrectKey) {
                                    Text(text = "✓ Correct Key", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                                } else if (userChosen) {
                                    Text(text = "✗ Your Choice", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // DETAILED EXPLANATION BOX
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Comprehensive Explanation",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentQ.sampleAnswer,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // STICKY BOTTOM BUTTONS IN REVIEW MODE
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { if (reviewIndex > 0) reviewIndex-- },
                    enabled = reviewIndex > 0,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Previous")
                }

                if (reviewIndex < quiz.questions.size - 1) {
                    Button(
                        onClick = { reviewIndex++ },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Next Question")
                    }
                } else {
                    Button(
                        onClick = onBackToDashboard,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Finish Review")
                    }
                }
            }
        }
    }
}
