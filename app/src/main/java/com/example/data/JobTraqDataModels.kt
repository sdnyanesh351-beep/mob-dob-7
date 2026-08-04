package com.example.data

data class JobEntity(
    val id: String,
    val companyName: String,
    val jobTitle: String,
    val status: String, // Saved, Applied, Interviewing, Offered, Rejected
    val notes: String = "",
    val salary: String = "",
    val location: String = "",
    val interviewDate: String = "",
    val hrName: String = "",
    val hrNumber: String = "",
    val hrEmail: String = "",
    val tenantId: String = "platform",
    val updatedAt: Long = System.currentTimeMillis()
)

data class QuestionEntity(
    val id: String,
    val questionText: String,
    val category: String, // Technical, Behavioral, HR, System Design
    val difficulty: String, // Easy, Medium, Hard
    val sampleAnswer: String,
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int = 0,
    val isBookmarked: Boolean = false
)

data class QuizEntity(
    val id: String,
    val title: String,
    val description: String,
    val questionCount: Int,
    val questions: List<QuestionEntity>
)

data class QuizResult(
    val quizTitle: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val scorePercentage: Int,
    val userAnswers: Map<String, Int>, // questionId -> chosenIndex
    val quiz: QuizEntity? = null,
    val durationSeconds: Int = 120,
    val isChallengeMode: Boolean = true,
    val markedForReviewIds: Set<String> = emptySet(),
    val bookmarkedIds: Set<String> = emptySet(),
    val questionRatings: Map<String, Int> = emptyMap(),
    val questionFeedbacks: Map<String, String> = emptyMap()
)

data class CommentEntity(
    val id: String,
    val authorName: String,
    val authorRole: String = "Member",
    val timestamp: String = "Just now",
    val text: String
)

data class PollOptionEntity(
    val option: String,
    val votes: Int = 0
)

data class BlogPostEntity(
    val id: String,
    val title: String,
    val content: String,
    val excerpt: String,
    val author: String,
    val date: String,
    val imageUrl: String? = null,
    val tags: List<String> = emptyList(),
    val bookmarkedBy: List<String> = emptyList()
)

data class FeedPostEntity(
    val id: String,
    val authorName: String,
    val authorRole: String,
    val content: String,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val timestamp: String = "Just now",
    val tenantId: String = "platform",
    val comments: List<CommentEntity> = emptyList(),
    val type: String = "Discussion",
    val pollOptions: List<PollOptionEntity> = emptyList(),
    val eventTitle: String? = null,
    val eventDate: String? = null,
    val eventLocation: String? = null
)

data class ResumeEntity(
    val id: String,
    val title: String,
    val targetRole: String,
    val content: String,
    val matchScore: Int = 0,
    val feedback: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

data class ResumeScanReportEntity(
    val id: String = java.util.UUID.randomUUID().toString(),
    val resumeTitle: String,
    val targetRole: String,
    val jobTitle: String,
    val companyName: String = "TechCorp Inc.",
    val scanDate: String = "Today, 2:15 PM",
    val matchScore: Int = 85,
    val matchingKeywords: List<String> = listOf("Kotlin", "Jetpack Compose", "Coroutines", "MVVM", "Room DB"),
    val missingKeywords: List<String> = listOf("CI/CD GitHub Actions", "Robolectric Unit Testing", "KSP", "API Caching"),
    val summaryFeedback: String = "Your resume demonstrates high technical proficiency for Android/Mobile positions. Incorporate missing keywords around automated testing and deployment pipelines to reach a 95%+ ATS score.",
    val actionItems: List<String> = listOf(
        "Quantify project metrics (e.g. 'Improved startup performance by 35%')",
        "Add CI/CD experience under key technical competencies",
        "Align headline directly with job posting title"
    )
)

data class WalletTransactionEntity(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: String, // "CREDIT", "DEBIT"
    val amount: Int,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ReferralHistoryEntity(
    val id: String = java.util.UUID.randomUUID().toString(),
    val referrerUserId: String = "user-alex-101",
    val referrerName: String = "Alex Rivera",
    val referredEmailOrName: String,
    val referralDate: Long = System.currentTimeMillis(),
    val status: String, // 'Pending', 'Signed Up', 'Reward Earned'
    val rewardAmount: Int? = null,
    val department: String = "Engineering",
    val jobTitle: String = "Software Engineer"
)

data class ReferralLeaderboardUser(
    val rank: Int,
    val userId: String,
    val name: String,
    val successfulReferrals: Int,
    val totalEarnedCoins: Int,
    val avatarBadgeIndex: Int = 0
)

data class ReferralActivityLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class WalletState(
    val coins: Int = 500,
    val flashCoins: Int = 50,
    val streakDays: Int = 7,
    val streakFreezes: Int = 1,
    val xp: Int = 1250,
    val level: Int = 5,
    val badges: List<String> = listOf("Early Adopter", "Profile Pro", "7-Day Streak", "Quiz Master"),
    val referralCode: String = "REF123",
    val transactions: List<WalletTransactionEntity> = listOf(
        WalletTransactionEntity(type = "CREDIT", amount = 50, description = "Referral Activation Bonus XP (+50 XP) - Jordan Smith"),
        WalletTransactionEntity(type = "CREDIT", amount = 700, description = "Referral Hired Bonus (Engineering) - Sarah Connor")
    )
)

data class AlumniUser(
    val name: String,
    val company: String,
    val role: String,
    val graduationYear: String,
    val availableForMentoring: Boolean = true
)

data class OfferComparisonEntity(
    val id: String,
    val companyName: String,
    val roleTitle: String,
    val baseSalary: Double,
    val bonusAmount: Double,
    val equityValueAnnual: Double,
    val signingBonus: Double = 0.0,
    val isRemote: Boolean = true,
    val location: String = "Remote",
    val matchScore: Int = 90,
    val notes: String = ""
) {
    val totalAnnualComp: Double
        get() = baseSalary + bonusAmount + equityValueAnnual + (signingBonus / 4.0)
}

data class AlumniMentorEntity(
    val id: String,
    val name: String,
    val company: String,
    val role: String,
    val location: String,
    val graduationYear: String,
    val bio: String,
    val skills: List<String>,
    val availableServices: List<String>,
    val rating: Double = 4.9,
    val reviewsCount: Int = 24
)

data class MockAnswerAnalysis(
    val starScore: Int,
    val situationFeedback: String,
    val taskFeedback: String,
    val actionFeedback: String,
    val resultFeedback: String,
    val strengths: List<String>,
    val improvements: List<String>,
    val polishedResponse: String
)

data class CoverLetterEntity(
    val id: String,
    val companyName: String,
    val jobTitle: String,
    val letterContent: String,
    val recruiterMessage: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class QuizChallengeEntity(
    val id: String,
    val code: String, // 6-character unique challenge code e.g. "AB3D9X"
    val creatorName: String,
    val quizTitle: String,
    val quizDescription: String,
    val questions: List<QuestionEntity>,
    val creatorScorePercentage: Int,
    val creatorCorrectAnswers: Int,
    val creatorTotalQuestions: Int,
    val creatorElapsedTimeSeconds: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val invitedFriends: List<String> = listOf("Alex Rivera", "Jordan Smith", "Taylor Chen")
)

data class QuizAttemptEntity(
    val id: String,
    val challengeCode: String,
    val userName: String,
    val scorePercentage: Int,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val elapsedTimeSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class DailyStreakNotificationState(
    val streakDays: Int = 7,
    val dailyChallengeCompletedToday: Boolean = false,
    val lastCompletedDate: String = "2026-07-22",
    val dailyReminderEnabled: Boolean = true,
    val reminderTime: String = "09:00 AM",
    val reminderFrequency: String = "Daily",
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val showSystemNotificationBanner: Boolean = false,
    val notificationBannerText: String = "🔥 Don't lose your 7-day streak! Complete today's interview question.",
    val streakHistoryDays: List<Boolean> = listOf(true, true, true, true, true, true, false) // M T W T F S S
)


