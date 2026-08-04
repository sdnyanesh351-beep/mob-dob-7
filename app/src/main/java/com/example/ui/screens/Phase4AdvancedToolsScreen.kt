package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CoverLetterEntity
import com.example.data.ResumeEntity
import com.example.data.ResumeScanHistoryEntity
import com.example.data.ResumeScanHistorySummaryStats
import com.example.data.buildReportDataJson
import com.example.data.computeSummaryStats
import com.example.data.extractActionItems
import com.example.data.extractMatchingKeywords
import com.example.data.extractMissingKeywords
import com.example.data.extractSummaryFeedback
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun Phase4AdvancedToolsScreen(
    resumes: List<ResumeEntity>,
    scanHistory: List<ResumeScanHistoryEntity> = emptyList(),
    coverLetters: List<CoverLetterEntity> = emptyList(),
    activityLogs: List<String> = emptyList(),
    onAddResume: (String, String, String) -> Unit,
    onAnalyzeResume: suspend (String, String) -> ResumeEntity,
    onGenerateCoverLetter: suspend (String, String, String, String) -> CoverLetterEntity = { _, _, _, _ ->
        CoverLetterEntity("", "", "", "")
    },
    onToggleScanBookmark: (String) -> Unit = {},
    onDeleteScan: (String) -> Unit = {},
    onViewScanReport: (ResumeScanHistoryEntity) -> Unit = {},
    onShowToast: (String) -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Resumes & AI ATS, 1: AI Cover Letters, 2: Scan History, 3: Activity Log
    val subTabs = listOf("Resumes & AI ATS", "AI Cover Letters & DM", "Scan History", "Activity Log")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            edgePadding = 16.dp
        ) {
            subTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    modifier = Modifier.testTag("tools_subtab_$index")
                )
            }
        }

        when (selectedSubTab) {
            0 -> ResumesAndAiSection(
                resumes = resumes,
                scanHistory = scanHistory,
                onAddResume = onAddResume,
                onAnalyzeResume = onAnalyzeResume,
                onToggleScanBookmark = onToggleScanBookmark,
                onDeleteScan = onDeleteScan,
                onViewScanReport = onViewScanReport,
                onShowToast = onShowToast
            )
            1 -> AICoverLetterSection(
                coverLetters = coverLetters,
                resumes = resumes,
                onGenerateCoverLetter = onGenerateCoverLetter,
                onShowToast = onShowToast
            )
            2 -> ResumeScanHistorySection(
                scanHistory = scanHistory,
                onToggleBookmark = onToggleScanBookmark,
                onDelete = onDeleteScan,
                onViewReport = onViewScanReport,
                onShowToast = onShowToast
            )
            3 -> ActivityLogSection(logs = activityLogs)
        }
    }
}


@Composable
private fun ResumesAndAiSection(
    resumes: List<ResumeEntity>,
    scanHistory: List<ResumeScanHistoryEntity> = emptyList(),
    onAddResume: (String, String, String) -> Unit,
    onAnalyzeResume: suspend (String, String) -> ResumeEntity,
    onToggleScanBookmark: (String) -> Unit = {},
    onDeleteScan: (String) -> Unit = {},
    onViewScanReport: (ResumeScanHistoryEntity) -> Unit = {},
    onShowToast: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isAddModalOpen by remember { mutableStateOf(false) }

    var selectedResumeForAnalysis by remember { mutableStateOf<ResumeEntity?>(resumes.firstOrNull()) }
    var jobDescriptionInput by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<ResumeEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // AI ATS Match Analyzer Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ats_analyzer_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gemini AI ATS Match Analyzer",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Select a saved resume profile and paste a job description to calculate match score & missing keywords.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Select Resume Dropdown
                var resumeDropdownExpanded by remember { mutableStateOf(false) }
                Box {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { resumeDropdownExpanded = true }
                            .testTag("select_resume_dropdown"),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedResumeForAnalysis?.title ?: "Select Resume Profile",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text("▼", fontSize = 12.sp)
                        }
                    }

                    DropdownMenu(
                        expanded = resumeDropdownExpanded,
                        onDismissRequest = { resumeDropdownExpanded = false }
                    ) {
                        resumes.forEach { res ->
                            DropdownMenuItem(
                                text = { Text(res.title) },
                                onClick = {
                                    selectedResumeForAnalysis = res
                                    resumeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = jobDescriptionInput,
                    onValueChange = { jobDescriptionInput = it },
                    placeholder = { Text("Paste Job Description here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("jd_input_field"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (selectedResumeForAnalysis != null && jobDescriptionInput.isNotBlank()) {
                            isAnalyzing = true
                            coroutineScope.launch {
                                val result = onAnalyzeResume(selectedResumeForAnalysis!!.id, jobDescriptionInput)
                                analysisResult = result
                                isAnalyzing = false
                                onShowToast("AI Analysis Complete!")
                            }
                        } else {
                            onShowToast("Please select a resume and enter job description")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("run_ats_analysis_button"),
                    enabled = !isAnalyzing,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyzing with Gemini...")
                    } else {
                        Text("Run ATS Match Analysis")
                    }
                }
            }
        }

        // Analysis Results Output Card
        if (analysisResult != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Match Score: ${analysisResult!!.matchScore}%",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = analysisResult!!.feedback,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Saved Resumes List
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Saved Resumes",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Button(
                onClick = { isAddModalOpen = true },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_resume_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Resume")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        resumes.forEach { res ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(res.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text("Target Role: ${res.targetRole}", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = res.content.take(120) + "...",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        if (scanHistory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recent Resume Scans",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = "View All (${scanHistory.size}) →",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    scanHistory.take(3).forEach { scan ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            scan.matchScore?.let { ScoreCircle(score = it, size = 40.dp, strokeWidth = 3.dp) }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (scan.jobTitle.isNotBlank()) scan.jobTitle else "(Untitled scan)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1
                                )
                                if (scan.companyName.isNotBlank()) {
                                    Text(scan.companyName, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                }
                                Text(
                                    formatRelativeDate(scan.scanDate),
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            IconButton(onClick = { onToggleScanBookmark(scan.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "bookmark",
                                    tint = if (scan.bookmarked) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (isAddModalOpen) {
        var title by remember { mutableStateOf("") }
        var role by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { isAddModalOpen = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Add New Resume Profile", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Target Role") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Resume Content") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { isAddModalOpen = false }) { Text("Cancel") }
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    onAddResume(title, role, content)
                                    isAddModalOpen = false
                                }
                            }
                        ) { Text("Save Resume") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityLogSection(logs: List<String>) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Activity Log", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(logs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(log, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

private fun formatRelativeDate(epochMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - epochMillis
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000} min ago"
        diff < 86_400_000 -> "${diff / 3_600_000} hr ago"
        diff < 7 * 86_400_000 -> "${diff / 86_400_000} days ago"
        else -> SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(epochMillis))
    }
}

@Composable
private fun ScoreCircle(
    score: Int,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 52.dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 4.dp
) {
    val clampedScore = score.coerceIn(0, 100)
    val progress = clampedScore / 100f
    val progressColor = when {
        clampedScore >= 80 -> Color(0xFF2E7D32)
        clampedScore >= 60 -> Color(0xFFF9A825)
        clampedScore >= 40 -> Color(0xFFEF6C00)
        else -> Color(0xFFC62828)
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(size),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = strokeWidth
        )
        Text(
            text = "$clampedScore",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = progressColor)
        )
    }
}

enum class ScanHistoryFilter(val label: String) {
    ALL("View All"),
    HIGHEST("Highest Match"),
    STARRED("Starred"),
    ARCHIVED("Archived (Mock)")
}

@Composable
private fun ResumeScanHistorySection(
    scanHistory: List<ResumeScanHistoryEntity>,
    onToggleBookmark: (String) -> Unit,
    onDelete: (String) -> Unit,
    onViewReport: (ResumeScanHistoryEntity) -> Unit,
    onShowToast: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf(ScanHistoryFilter.ALL) }
    var reportForScan by remember { mutableStateOf<ResumeScanHistoryEntity?>(null) }
    val stats = remember(scanHistory) { scanHistory.computeSummaryStats() }

    val filteredScans = remember(scanHistory, selectedFilter) {
        when (selectedFilter) {
            ScanHistoryFilter.ALL -> scanHistory
            ScanHistoryFilter.HIGHEST -> scanHistory.sortedByDescending { it.matchScore ?: 0 }
            ScanHistoryFilter.STARRED -> scanHistory.filter { it.bookmarked }
            ScanHistoryFilter.ARCHIVED -> emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header + stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Resume Scan History",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Text(
                "${filteredScans.size} results",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Stats grid (2x2)
        androidx.compose.foundation.layout.BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val cardWidth = (maxWidth - 8.dp) / 2
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    ScanHistorySummaryStatCard(
                        label = "Total Scans",
                        value = stats.totalScans.toString(),
                        icon = Icons.Default.History,
                        modifier = Modifier.width(cardWidth)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ScanHistorySummaryStatCard(
                        label = "Unique Resumes",
                        value = stats.uniqueResumes.toString(),
                        icon = Icons.Default.Description,
                        modifier = Modifier.width(cardWidth)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    ScanHistorySummaryStatCard(
                        label = "Highest Score",
                        value = "${stats.highestScore}%",
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.width(cardWidth),
                        accentColor = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ScanHistorySummaryStatCard(
                        label = "High Scoring (≥80%)",
                        value = stats.highScoringCount.toString(),
                        icon = Icons.Default.AutoAwesome,
                        modifier = Modifier.width(cardWidth),
                        accentColor = Color(0xFF1565C0)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScrollCompat(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScanHistoryFilter.values().forEach { filter ->
                val selected = selectedFilter == filter
                AssistChip(
                    onClick = { selectedFilter = filter },
                    label = {
                        Text(
                            text = filter.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    leadingIcon = if (selected) {
                        { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary) }
                    } else null,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Item list
        if (filteredScans.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No scans found.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Run an ATS Match Analysis to populate history.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredScans, key = { it.id }) { scan ->
                    var showMenu by remember { mutableStateOf(false) }
                    Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItemPlacementCompat(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (scan.bookmarked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                scan.matchScore?.let {
                                    ScoreCircle(score = it)
                                } ?: run {
                                    Box(
                                        modifier = Modifier.size(52.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Description,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (scan.jobTitle.isNotBlank()) scan.jobTitle else "(Untitled job)",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1
                                        )
                                        if (scan.bookmarked) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "bookmarked",
                                                tint = Color(0xFFFFC107),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    if (scan.companyName.isNotBlank()) {
                                        Text(
                                            scan.companyName,
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = buildString {
                                            if (scan.resumeName.isNotBlank()) append("• ${scan.resumeName}")
                                            append("  •  ")
                                            append(formatRelativeDate(scan.scanDate))
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = { onToggleBookmark(scan.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "toggle bookmark",
                                        tint = if (scan.bookmarked) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box {
                                    IconButton(onClick = { showMenu = true }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "scan options")
                                    }
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("View Report") },
                                            leadingIcon = { Icon(Icons.Default.OpenInNew, contentDescription = null) },
                                            onClick = {
                                                showMenu = false
                                                reportForScan = scan
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "Delete Scan",
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            },
                                            onClick = {
                                                showMenu = false
                                                onDelete(scan.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                }
            }
        }
    }

    if (reportForScan != null) {
        val scan = reportForScan!!
        ScanReportDialog(
            scan = scan,
            onDismiss = { reportForScan = null }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScanReportDialog(
    scan: ResumeScanHistoryEntity,
    onDismiss: () -> Unit
) {
    val matching = remember(scan) { scan.extractMatchingKeywords() }
    val missing = remember(scan) { scan.extractMissingKeywords() }
    val summary = remember(scan) { scan.extractSummaryFeedback() }
    val actions = remember(scan) { scan.extractActionItems() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Scan Report",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    scan.matchScore?.let { ScoreCircle(score = it, size = 44.dp, strokeWidth = 3.dp) }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (scan.jobTitle.isNotBlank()) "${scan.jobTitle}${if (scan.companyName.isNotBlank()) " @ ${scan.companyName}" else ""}"
                    else "(Untitled)",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(14.dp))

                if (summary.isNotBlank()) {
                    Text("Summary", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (matching.isNotEmpty()) {
                    Text(
                        "Matching Keywords (${matching.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        matching.forEach { kw ->
                            AssistChip(
                                onClick = {},
                                label = { Text(kw) },
                                shape = RoundedCornerShape(10.dp),
                                colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                                    containerColor = Color(0xFFE8F5E9)
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (missing.isNotEmpty()) {
                    Text(
                        "Missing Keywords (${missing.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        missing.forEach { kw ->
                            AssistChip(
                                onClick = {},
                                label = { Text(kw) },
                                shape = RoundedCornerShape(10.dp),
                                colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                                    containerColor = Color(0xFFFFEBEE)
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (actions.isNotEmpty()) {
                    Text(
                        "Action Items",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    actions.forEachIndexed { idx, action ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    (idx + 1).toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(action, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (summary.isBlank() && matching.isEmpty() && missing.isEmpty() && actions.isEmpty()) {
                    Text(
                        "No structured report data stored for this scan.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
            }
        }
    }
}

@Composable
private fun ScanHistorySummaryStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = accentColor)
            )
        }
    }
}

@Composable
private fun Modifier.horizontalScrollCompat(): Modifier = this.then(
    horizontalScroll(rememberScrollState())
)

@Composable
private fun Modifier.animateItemPlacementCompat(): Modifier = this

@Composable
private fun AICoverLetterSection(
    coverLetters: List<CoverLetterEntity>,
    resumes: List<ResumeEntity>,
    onGenerateCoverLetter: suspend (String, String, String, String) -> CoverLetterEntity,
    onShowToast: (String) -> Unit
) {
    var companyName by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var jobDescription by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

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
                    text = "✉️ AI Cover Letter & Outreach Generator",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                Text(
                    text = "Craft highly targeted, professional cover letters and LinkedIn recruiter outreach DMs powered by Gemini AI.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = companyName,
            onValueChange = { companyName = it },
            label = { Text("Company Name") },
            placeholder = { Text("e.g. TechCorp Inc") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = jobTitle,
            onValueChange = { jobTitle = it },
            label = { Text("Target Job Title") },
            placeholder = { Text("e.g. Senior Mobile Engineer") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = jobDescription,
            onValueChange = { jobDescription = it },
            label = { Text("Job Requirements / Keywords") },
            placeholder = { Text("Paste job responsibilities or key tech stack requirements...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                if (companyName.isBlank() || jobTitle.isBlank()) {
                    onShowToast("Please enter company name and job title.")
                    return@Button
                }
                isGenerating = true
                coroutineScope.launch {
                    val resumeText = resumes.firstOrNull()?.content ?: "Software Engineer with experience in Kotlin and Compose."
                    onGenerateCoverLetter(companyName, jobTitle, jobDescription, resumeText)
                    isGenerating = false
                    onShowToast("Cover Letter generated successfully!")
                    companyName = ""
                    jobTitle = ""
                    jobDescription = ""
                }
            },
            enabled = !isGenerating,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("generate_cover_letter_button")
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generating with Gemini AI...")
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate Cover Letter & Outreach DM", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Saved Cover Letters & Outreach Messages:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(10.dp))

        coverLetters.forEach { letter ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("cover_letter_card_${letter.id}"),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = letter.companyName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = letter.jobTitle,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        IconButton(onClick = { onShowToast("Cover Letter copied to clipboard!") }) {
                            Icon(Icons.Default.Share, contentDescription = "Copy")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = letter.letterContent,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    )

                    if (letter.recruiterMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "💬 LinkedIn / Email Recruiter DM:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = letter.recruiterMessage, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

