package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resume_scan_history")
data class ResumeScanHistoryEntity(
    @PrimaryKey val id: String,
    val tenantId: String = "platform",
    val userId: String = "",
    val resumeId: String = "",
    val resumeName: String = "",
    val jobTitle: String = "",
    val companyName: String = "",
    val scanDate: Long = System.currentTimeMillis(),
    val matchScore: Int? = null,
    val resumeTextSnapshot: String = "",
    val jobDescriptionText: String = "",
    val reportDataJson: String? = null,
    val bookmarked: Boolean = false
)
