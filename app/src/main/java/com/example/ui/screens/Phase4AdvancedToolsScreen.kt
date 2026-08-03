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
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
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
import com.example.data.ResumeScanReportEntity
import kotlinx.coroutines.launch


@Composable
fun Phase4AdvancedToolsScreen(
    resumes: List<ResumeEntity>,
    scanHistory: List<ResumeScanReportEntity> = emptyList(),
    coverLetters: List<CoverLetterEntity> = emptyList(),
    onAddResume: (String, String, String) -> Unit,
    onAnalyzeResume: suspend (String, String) -> ResumeEntity,
    onGenerateCoverLetter: suspend (String, String, String, String) -> CoverLetterEntity = { _, _, _, _ ->
        CoverLetterEntity("", "", "", "")
    },
    onShowToast: (String) -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Resumes & AI ATS, 1: AI Cover Letters, 2: Activity Log
    val subTabs = listOf("Resumes & AI ATS", "AI Cover Letters", "Activity Log")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            subTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = { Text(title, fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) },
                    modifier = Modifier.testTag("tools_subtab_$index")
                )
            }
        }

        when (selectedSubTab) {
            0 -> ResumesAndAiSection(
                resumes = resumes,
                onAddResume = onAddResume,
                onAnalyzeResume = onAnalyzeResume,
                onShowToast = onShowToast
            )
            1 -> AICoverLetterSection(
                coverLetters = coverLetters,
                resumes = resumes,
                onGenerateCoverLetter = onGenerateCoverLetter,
                onShowToast = onShowToast
            )
            2 -> ActivityLogSection()
        }
    }
}


@Composable
private fun ResumesAndAiSection(
    resumes: List<ResumeEntity>,
    onAddResume: (String, String, String) -> Unit,
    onAnalyzeResume: suspend (String, String) -> ResumeEntity,
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
private fun ActivityLogSection() {

    val logs = listOf(
        "Logged in via Secure JWT Auth",
        "Submitted Daily Interview Challenge (+50 XP)",
        "Added new Job Application: 'TechCorp Inc'",
        "Updated Job Status to 'Interviewing'",
        "Ran Gemini AI ATS Resume Analysis"
    )

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
                    text = "✉️ AI Cover Letter Generator",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                Text(
                    text = "Craft highly targeted, professional cover letters powered by Gemini AI.",
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
                Text("Generate Cover Letter", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Saved Cover Letters:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
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

