package com.example.data

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

// --- Auth DTOs ---
data class ApiSignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "user",
    val referralCode: String? = null,
    val tenantId: String = "platform"
)

data class ApiLoginRequest(
    val email: String,
    val password: String,
    val tenantId: String = "platform"
)

data class ApiGoogleAuthRequest(
    val idToken: String,
    val action: String,
    val tenantId: String? = null,
    val referralCode: String? = null,
    val partnerCode: String? = null
)

data class ApiUserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val tenantId: String = "platform"
)

data class ApiAuthResponse(
    val token: String? = null,
    val user: ApiUserDto? = null,
    val message: String? = null
)

// --- Wallet & Subscriptions DTOs ---
data class ApiWalletDto(
    val coins: Int,
    val flashCoins: Int,
    val xp: Int,
    val streakFreezes: Int,
    val referralCode: String
)

data class ApiWalletTransactionDto(
    val id: String,
    val type: String, // "CREDIT", "DEBIT"
    val amount: Int,
    val description: String,
    val timestamp: String
)

data class ApiWalletResponse(
    val wallet: ApiWalletDto,
    val transactions: List<ApiWalletTransactionDto>
)

data class ApiWalletPostRequest(
    val action: String,
    val code: String? = null
)

data class ApiCreateOrderRequest(
    val amount: Int
)

data class ApiOrderResponse(
    val id: String,
    val amount: Int,
    val currency: String = "INR",
    val status: String = "created"
)

data class ApiPaymentVerifyRequest(
    val razorpay_order_id: String,
    val razorpay_payment_id: String,
    val razorpay_signature: String,
    val amount: Int
)

data class ApiCreateSubscriptionRequest(
    val tier: String // e.g. "GOLD"
)

data class ApiSubscriptionResponse(
    val subscriptionId: String,
    val status: String
)

// --- Quizzes & Challenges DTOs ---
data class ApiQuizProgressRequest(
    val sessionId: String,
    val currentQuestionIndex: Int,
    val timeLeft: Int,
    val answers: Map<String, String>,
    val questions: List<String> = emptyList()
)

data class ApiCompleteQuizRequest(
    val sessionId: String,
    val score: Int,
    val percentage: Double,
    val timeTaken: Int,
    val categoryStats: Map<String, Any>? = null,
    val tagStats: Map<String, Any>? = null
)

data class ApiCreateChallengeRequest(
    val quizSessionId: String,
    val title: String,
    val description: String,
    val challengedUserIds: List<String>
)

data class ApiChallengeResponse(
    val challengeCode: String,
    val status: String = "created"
)

// --- AI Mock Interviews DTOs ---
data class ApiCreateInterviewRequest(
    val topic: String,
    val description: String,
    val difficulty: String,
    val timerPerQuestion: Int,
    val questionCategories: List<String>,
    val instantFeedback: Boolean
)

// --- Question Bank DTOs ---
data class ApiBookmarkRequest(
    val questionId: String
)

// --- Referrals DTOs ---
data class ApiNudgeRequest(
    val referredUserId: String
)

data class ApiGiftShieldRequest(
    val referredUserId: String
)

// --- Community DTOs ---
data class ApiPollOptionDto(
    val option: String,
    val votes: Int = 0
)

data class ApiCreatePostRequest(
    val title: String? = null,
    val content: String,
    val type: String, // "text", "poll", "event", "request"
    val tags: List<String>? = null,
    val imageUrl: String? = null,
    val attachmentUrl: String? = null,
    val pollOptions: List<ApiPollOptionDto>? = null,
    val eventTitle: String? = null,
    val eventDate: String? = null,
    val eventLocation: String? = null,
    val attendees: Int? = 0,
    val capacity: Int? = 0,
    val assignedTo: String? = null,
    val status: String? = null
)

data class ApiCreateCommentRequest(
    val postId: String,
    val text: String
)

data class ApiToggleLikeRequest(
    val postId: String
)

data class ApiPollVoteRequest(
    val postId: String,
    val optionIndex: Int,
    val optionId: Int? = null,
    val option: String? = null
)

data class ApiEventRegisterRequest(
    val postId: String
)

data class ApiRequestAssignRequest(
    val postId: String
)

data class ApiCreateResumeRequest(
    val title: String,
    val content: String
)

// --- Resume Scan History DTOs ---
data class ApiResumeScanHistoryDto(
    val id: String? = null,
    val tenantId: String? = null,
    val userId: String? = null,
    val resumeId: String? = null,
    val resumeName: String? = null,
    val jobTitle: String? = null,
    val companyName: String? = null,
    val scanDate: String? = null,
    val matchScore: Int? = null,
    val resumeTextSnapshot: String? = null,
    val jobDescriptionText: String? = null,
    val reportData: Any? = null,
    val bookmarked: Boolean? = null
)

data class ApiResumeScanHistoryResponse(
    val success: Boolean = true,
    val message: String? = null,
    val data: List<ApiResumeScanHistoryDto>? = null,
    val scanHistory: List<ApiResumeScanHistoryDto>? = null
)

data class ApiUpdateScanBookmarkRequest(
    val scanId: String,
    val bookmarked: Boolean
)

data class ApiCreateScanHistoryRequest(
    val resumeId: String,
    val resumeName: String,
    val jobTitle: String,
    val companyName: String,
    val matchScore: Int,
    val resumeTextSnapshot: String,
    val jobDescriptionText: String,
    val reportData: Any? = null
)

data class ApiRateQuestionRequest(
    val questionId: String,
    val rating: Int
)

data class ApiCommentQuestionRequest(
    val questionId: String,
    val commentText: String
)

// --- Blog DTOs ---
data class ApiBlogPostDto(
    val id: String,
    val title: String,
    val slug: String,
    val content: String,
    val excerpt: String,
    val author: String,
    val date: String,
    val imageUrl: String? = null,
    val tags: List<String>? = emptyList(),
    val bookmarkedBy: List<String>? = emptyList()
)

data class ApiBlogResponse(
    val success: Boolean = true,
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 20,
    val totalPages: Int = 1,
    val data: List<ApiBlogPostDto>? = emptyList()
)

data class ApiCreateBlogPostRequest(
    val title: String,
    val content: String,
    val excerpt: String,
    val tags: List<String>? = emptyList(),
    val imageUrl: String? = null
)

data class ApiBlogBookmarkRequest(
    val postId: String
)

// --- Job Applications DTOs ---
data class ApiJobApplicationRequest(
    val title: String,
    val company: String,
    val status: String,
    val location: String,
    val url: String,
    val salaryRange: String,
    val notes: String
)

data class ApiUpdateJobApplicationRequest(
    val applicationId: String,
    val updateData: Map<String, Any>
)

// --- Profile & Settings DTOs ---
data class ApiUpdateSettingsRequest(
    val challengeTopics: List<String>? = null,
    val userApiKey: String? = null
)

// --- User Profile DTOs (Phase 1.2) ---
data class ApiUserProfileDto(
    val id: String? = null,
    val name: String? = null,
    val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val headline: String? = null,
    val location: String? = null,
    val skills: List<String>? = emptyList(),
    val education: List<Any?>? = emptyList(),
    val experience: List<Any?>? = emptyList(),
    val resumeUrl: String? = null,
    val preferredRole: String? = null,
    val expectedSalary: String? = null,
    val language: String? = "en",
    val avatarBadgeIndex: Int? = 0,
    val createdAt: String? = null,
    val tenantId: String? = null,
    val role: String? = null
)

data class ApiUpdateProfileRequest(
    val name: String? = null,
    val fullName: String? = null,
    val phone: String? = null,
    val headline: String? = null,
    val location: String? = null,
    val skills: List<String>? = null,
    val education: List<Any?>? = null,
    val experience: List<Any?>? = null,
    val resumeUrl: String? = null,
    val preferredRole: String? = null,
    val expectedSalary: String? = null,
    val avatarBadgeIndex: Int? = null
)

data class ApiProfileCompletionCheckItem(
    val field: String,
    val label: String,
    val completed: Boolean
)

data class ApiProfileCompletionResponse(
    val success: Boolean = true,
    val percent: Int = 0,
    val total: Int = 0,
    val completed: Int = 0,
    val checklist: List<ApiProfileCompletionCheckItem> = emptyList()
)

data class ApiLeaderboardEntryDto(
    val rank: Int,
    val userId: String,
    val name: String,
    val xp: Int? = 0,
    val successfulReferrals: Int? = 0,
    val totalEarnedCoins: Int? = 0,
    val streakDays: Int? = 0,
    val avatarBadgeIndex: Int? = 0
)

data class ApiLeaderboardResponse(
    val success: Boolean = true,
    val data: List<ApiLeaderboardEntryDto>? = emptyList(),
    val leaderboard: List<ApiLeaderboardEntryDto>? = emptyList(),
    val userRank: Int? = null
)

// --- Daily Streak DTOs (Phase 1.2) ---
data class ApiStreakResponse(
    val success: Boolean = true,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActiveDate: String? = null,
    val streakFrozen: Boolean = false,
    val streakFreezesAvailable: Int = 0,
    val isActiveToday: Boolean = false,
    val weeklyPattern: List<Boolean> = listOf(false, false, false, false, false, false, false),
    val totalActiveDays: Int = 0
)

data class ApiQuestionRatingDto(
    val questionId: String,
    val avgRating: Double = 0.0,
    val ratingCount: Int = 0,
    val userRating: Int = 0
)

// --- Standard Generic Response ---
data class ApiStandardResponse(
    val success: Boolean = true,
    val message: String? = null
)

// --- Retrofit Service Interface ---
interface JobTraqMobileApiService {

    // 1. Authentication
    @POST("api/auth/signup")
    suspend fun signup(@Body request: ApiSignupRequest): Response<ApiAuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: ApiLoginRequest): Response<ApiAuthResponse>

    @POST("api/auth/google")
    suspend fun googleAuth(@Body request: ApiGoogleAuthRequest): Response<ApiAuthResponse>

    @GET("api/auth/verify")
    suspend fun verifyAccount(
        @Query("token") token: String,
        @Query("lang") lang: String? = null
    ): Response<ApiStandardResponse>

    // 1b. User Profile Endpoints
    @GET("api/users/me")
    suspend fun getUserMe(@Query("lang") lang: String? = null): Response<ResponseBody>

    @PUT("api/users/me")
    suspend fun updateUserMe(@Body request: ApiUpdateProfileRequest): Response<ApiStandardResponse>

    @GET("api/users/me/completion")
    suspend fun getUserProfileCompletion(@Query("lang") lang: String? = null): Response<ResponseBody>

    // 1c. Daily Streak Endpoints
    @GET("api/streak")
    suspend fun getStreak(@Query("lang") lang: String? = null): Response<ResponseBody>

    @POST("api/streak/record")
    suspend fun recordStreakAction(@Query("type") type: String): Response<ApiStandardResponse>

    // 2. Wallet & Subscriptions
    @GET("api/wallet")
    suspend fun getWallet(@Query("lang") lang: String? = null): Response<ResponseBody>

    @POST("api/wallet")
    suspend fun postWalletAction(@Body request: ApiWalletPostRequest): Response<ApiStandardResponse>

    @POST("api/payments/order")
    suspend fun createPaymentOrder(@Body request: ApiCreateOrderRequest): Response<ApiOrderResponse>

    @POST("api/payments/verify")
    suspend fun verifyPayment(@Body request: ApiPaymentVerifyRequest): Response<ApiStandardResponse>

    @POST("api/subscriptions/create")
    suspend fun createSubscription(@Body request: ApiCreateSubscriptionRequest): Response<ApiSubscriptionResponse>

    @POST("api/subscriptions/cancel")
    suspend fun cancelSubscription(): Response<ApiStandardResponse>

    // 3. Quizzes & Challenges
    @GET("api/quizzes")
    suspend fun getQuizzes(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("lang") lang: String? = null
    ): Response<ResponseBody>

    @POST("api/quizzes/progress")
    suspend fun saveQuizProgress(@Body request: ApiQuizProgressRequest): Response<ApiStandardResponse>

    @POST("api/quizzes/complete")
    suspend fun completeQuiz(@Body request: ApiCompleteQuizRequest): Response<ApiStandardResponse>

    @POST("api/challenges/create")
    suspend fun createChallenge(@Body request: ApiCreateChallengeRequest): Response<ApiChallengeResponse>

    // 4. AI Mock Interviews
    @GET("api/interviews")
    suspend fun getInterviews(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("lang") lang: String? = null
    ): Response<ResponseBody>

    @POST("api/interviews/create")
    suspend fun createInterview(@Body request: ApiCreateInterviewRequest): Response<ApiStandardResponse>

    // 5. Question Bank
    @GET("api/questions")
    suspend fun getQuestions(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("sort") sort: String? = "newest",
        @Query("difficulty") difficulty: String? = null,
        @Query("category") category: String? = null,
        @Query("lang") lang: String? = null
    ): Response<ResponseBody>

    @GET("api/questions/{id}/rating")
    suspend fun getQuestionRating(
        @retrofit2.http.Path("id") id: String,
        @Query("lang") lang: String? = null
    ): Response<ResponseBody>

    @POST("api/questions/bookmark")
    suspend fun bookmarkQuestion(@Body request: ApiBookmarkRequest): Response<ApiStandardResponse>

    @POST("api/questions/rate")
    suspend fun rateQuestion(@Body request: ApiRateQuestionRequest): Response<ApiStandardResponse>

    @POST("api/questions/comment")
    suspend fun commentQuestion(@Body request: ApiCommentQuestionRequest): Response<ApiStandardResponse>

    // 6. Referrals & Engagement
    @GET("api/referrals/history")
    suspend fun getReferralHistory(@Query("lang") lang: String? = null): Response<ResponseBody>

    @GET("api/referrals/leaderboard")
    suspend fun getReferralLeaderboard(
        @Query("tenant") tenant: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("lang") lang: String? = null
    ): Response<ResponseBody>

    @POST("api/referrals/nudge")
    suspend fun nudgeReferralFriend(@Body request: ApiNudgeRequest): Response<ApiStandardResponse>

    @POST("api/referrals/gift")
    suspend fun giftStreakProtectShield(@Body request: ApiGiftShieldRequest): Response<ApiStandardResponse>

    // 7. Community Social Feed
    @GET("api/community/posts")
    suspend fun getCommunityPosts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("lang") lang: String? = null
    ): Response<ResponseBody>

    @POST("api/community/posts")
    suspend fun createCommunityPost(@Body request: ApiCreatePostRequest): Response<ApiStandardResponse>

    @POST("api/community/comments")
    suspend fun addCommunityComment(@Body request: ApiCreateCommentRequest): Response<ApiStandardResponse>

    @POST("api/community/posts/like")
    suspend fun toggleCommunityPostLike(@Body request: ApiToggleLikeRequest): Response<ApiStandardResponse>

    @POST("api/community/polls/vote")
    suspend fun voteCommunityPoll(@Body request: ApiPollVoteRequest): Response<ApiStandardResponse>

    @POST("api/community/events/register")
    suspend fun toggleCommunityEventRegistration(@Body request: ApiEventRegisterRequest): Response<ApiStandardResponse>

    @POST("api/community/requests/assign")
    suspend fun assignCommunityRequestToMe(@Body request: ApiRequestAssignRequest): Response<ApiStandardResponse>

    @GET("api/resumes")
    suspend fun getResumes(@Query("lang") lang: String? = null): Response<ResponseBody>

    @POST("api/resumes")
    suspend fun createResume(@Body request: ApiCreateResumeRequest): Response<ApiStandardResponse>

    // Resume Scan History endpoints
    @GET("api/resumes/scan-history")
    suspend fun getScanHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
        @Query("sort") sort: String? = null,
        @Query("lang") lang: String? = null
    ): Response<ResponseBody>

    @POST("api/resumes/scan-history")
    suspend fun createScanHistory(@Body request: ApiCreateScanHistoryRequest): Response<ApiStandardResponse>

    @PUT("api/resumes/scan-history")
    suspend fun updateScanBookmark(@Body request: ApiUpdateScanBookmarkRequest): Response<ApiStandardResponse>

    @DELETE("api/resumes/scan-history")
    suspend fun deleteScanHistory(@Query("scanId") scanId: String): Response<ApiStandardResponse>

    @GET("api/tenants")
    suspend fun getTenants(@Query("lang") lang: String? = null): Response<ResponseBody>

    // 8. Job Applications Tracker
    @GET("api/jobs/applications")
    suspend fun getJobApplications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("lang") lang: String? = null
    ): Response<ResponseBody>

    @POST("api/jobs/applications")
    suspend fun addJobApplication(@Body request: ApiJobApplicationRequest): Response<ApiStandardResponse>

    @PUT("api/jobs/applications")
    suspend fun updateJobApplication(@Body request: ApiUpdateJobApplicationRequest): Response<ApiStandardResponse>

    @DELETE("api/jobs/applications")
    suspend fun deleteJobApplication(@Query("applicationId") applicationId: String): Response<ApiStandardResponse>

    // 9. Profile, Settings & Dashboard
    @GET("api/settings")
    suspend fun getSettings(@Query("lang") lang: String? = null): Response<ResponseBody>

    @POST("api/settings")
    suspend fun updateSettings(@Body request: ApiUpdateSettingsRequest): Response<ApiStandardResponse>

    @GET("api/profile/activities")
    suspend fun getProfileActivities(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("lang") lang: String? = null
    ): Response<ResponseBody>

    @GET("api/dashboard")
    suspend fun getDashboardSummary(@Query("lang") lang: String? = null): Response<ApiDashboardSummaryResponse>

    // 10. Blog & Insights
    @GET("api/blog")
    suspend fun getBlogPosts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("lang") lang: String? = null
    ): Response<ApiBlogResponse>

    @POST("api/blog")
    suspend fun createBlogPost(@Body request: ApiCreateBlogPostRequest): Response<ApiStandardResponse>

    @POST("api/blog/bookmark")
    suspend fun bookmarkBlogPost(@Body request: ApiBlogBookmarkRequest): Response<ApiStandardResponse>
}

// --- Profile / Dashboard DTOs (for Post-Login Progress Screen) ---
// Matches actual backend response: { success: true, message?, error?, data: { users, activities, badges, ... } }
// Reference: web/src/app/api/dashboard/route.ts + dashboard.ts getDashboardData()

data class ApiCoinStatsDto(
    val totalInCirculation: Int? = 0,
    val totalEarned: Int? = 0,
    val totalSpent: Int? = 0,
    val totalFeesCollected: Int? = 0,
    val topEarners: List<Any?>? = emptyList(),
    val topSpenders: List<Any?>? = emptyList(),
    val spendingByCategory: List<Any?>? = emptyList()
)

data class ApiAiUsageStatsDto(
    val resumeScans: Int? = 0,
    val mockInterviews: Int? = 0
)

data class ApiDashboardDataDto(
    val users: List<Any?>? = emptyList(),
    val tenants: List<Any?>? = emptyList(),
    val resumeScans: List<Any?>? = emptyList(),
    val communityPosts: List<Any?>? = emptyList(),
    val jobApplications: List<Any?>? = emptyList(),
    val appointments: List<Any?>? = emptyList(),
    val activities: List<Any?>? = emptyList(),
    val badges: List<Any?>? = emptyList(),
    val promotions: List<Any?>? = emptyList(),
    val challenges: List<Any?>? = emptyList(),
    val alumni: List<Any?>? = emptyList(),
    val mockInterviews: List<Any?>? = emptyList(),
    val systemAlerts: List<Any?>? = emptyList(),
    val affiliates: List<Any?>? = emptyList(),
    val featureRequests: List<Any?>? = emptyList(),
    val blogPosts: List<Any?>? = emptyList(),
    val batches: List<Any?>? = emptyList(),
    val placementDrives: List<Any?>? = emptyList(),
    val certificates: List<Any?>? = emptyList(),
    val assignments: List<Any?>? = emptyList(),
    val coinStats: ApiCoinStatsDto? = ApiCoinStatsDto(),
    val aiUsageStats: ApiAiUsageStatsDto? = ApiAiUsageStatsDto()
)

data class ApiLevelProgressDto(
    val level: Int = 1,
    val currentXp: Int = 0,
    val xpForNextLevel: Int = 1000,
    val rank: Int = 0,
    val percentToNext: Float = 0f,
    val dayStreak: Int = 0,
    val badgesEarned: Int = 0,
    val remainingXp: Int = 0
)

data class ApiDashboardSummaryResponse(
    val success: Boolean = true,
    val message: String? = null,
    val error: String? = null,
    val data: ApiDashboardDataDto? = null,
    val progress: ApiLevelProgressDto? = null // preserved for backwards-compat / future
)

fun ApiDashboardDataDto?.deriveLevelProgress(): ApiLevelProgressDto {
    val activitiesCount = this?.activities?.size ?: 0
    val badgesCount = this?.badges?.size ?: 0
    val mockInterviews = this?.mockInterviews?.size ?: 0
    val resumeScans = this?.resumeScans?.size ?: 0
    val coinsEarned = this?.coinStats?.totalEarned ?: 0

    val rawXp = (activitiesCount * 25) + (badgesCount * 100) + (mockInterviews * 150) + (resumeScans * 50) + coinsEarned
    val level = 1 + (rawXp / 1000)
    val xpForLevel = level * 1000
    val currentXpInLevel = rawXp - ((level - 1) * 1000)
    val xpForNext = 1000
    val percent = if (xpForNext > 0) (currentXpInLevel.toFloat() / xpForNext) * 100f else 0f
    val rank = 1000 - ((activitiesCount * 3 + badgesCount * 10 + mockInterviews * 8) % 999)

    return ApiLevelProgressDto(
        level = level.coerceIn(1, 99),
        currentXp = currentXpInLevel.coerceAtLeast(0),
        xpForNextLevel = xpForNext,
        rank = rank.coerceAtLeast(1),
        percentToNext = percent.coerceIn(0f, 99.9f),
        dayStreak = (activitiesCount / 7).coerceAtMost(365),
        badgesEarned = badgesCount,
        remainingXp = (xpForNext - currentXpInLevel).coerceAtLeast(0)
    )
}
