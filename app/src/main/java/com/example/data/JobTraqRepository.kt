package com.example.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant.parse
import java.util.UUID

class JobTraqRepository(
    private val sessionManager: SessionManager? = null,
    private val resumeScanHistoryDao: ResumeScanHistoryDao? = null,
    private val userDao: UserDao? = null,
    private val streakDataStoreManager: StreakDataStoreManager? = null
) {

    private val apiScope = CoroutineScope(Dispatchers.IO)

    private val _currentUserDisplayName = MutableStateFlow<String?>(null)

    fun setCurrentUserDisplayName(name: String) {
        _currentUserDisplayName.value = name.ifBlank { null }
    }

    // Active Environment Profile (Default active profile is DEV with real data)
    private val _currentEnvironment = MutableStateFlow(AppEnvironment.DEV)
    val currentEnvironment: StateFlow<AppEnvironment> = _currentEnvironment.asStateFlow()

    fun setEnvironment(env: AppEnvironment) {
        _currentEnvironment.value = env
        if (env.defaultBaseUrl.isNotBlank()) {
            _baseUrl.value = env.defaultBaseUrl
        }
        if (env == AppEnvironment.TEST) {
            loadTestDummyData()
        } else {
            clearAllLocalDummyData()
            apiScope.launch {
                fetchAllFromApi()
            }
        }
    }

    private fun clearAllLocalDummyData() {
        _jobs.value = emptyList()
        _questions.value = emptyList()
        _quizzes.value = emptyList()
        _recentQuizResults.value = emptyList()
        _challenges.value = emptyList()
        _attempts.value = emptyList()
        _feedPosts.value = emptyList()
        _resumes.value = emptyList()
        _resumeScanHistory.value = emptyList()
        _offers.value = emptyList()
        _alumniMentors.value = emptyList()
        _blogPosts.value = emptyList()
        _coverLetters.value = emptyList()
        _referrals.value = emptyList()
        _referralLeaderboard.value = emptyList()
        _referralActivityLogs.value = emptyList()
    }

    suspend fun fetchAllFromApi() {
        if (_currentEnvironment.value.isDummyDataAllowed) return
        val base = _baseUrl.value
        if (base.isBlank()) return
        fetchJobsFromApi(base)
        fetchQuestionsFromApi(base)
        fetchQuizzesFromApi(base)
        fetchCommunityPostsFromApi(base)
        fetchReferralHistoryFromApi(base)
        fetchLeaderboardFromApi(base)
        fetchWalletFromApi(base)
        fetchSettingsFromApi(base)
        fetchResumesFromApi(base)
        fetchScanHistoryFromApi(base)
        fetchDashboardDataFromApi(base)
        fetchProfileFromApi(base)
        fetchStreakFromApi(base)
        fetchProfileActivitiesFromApi(base)
    }

    suspend fun fetchJobsFromApi(baseUrl: String = _baseUrl.value) = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) return@withContext
        try {
            val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
            fetchJobsFromApi(apiService)
        } catch (e: Exception) {
            // For DEV/PROD without backend yet, allow empty state without crashing
            if (!_currentEnvironment.value.isDummyDataAllowed) {
                // Leave empty, will be populated as user adds items
            }
        }
    }

    suspend fun fetchQuestionsFromApi(baseUrl: String = _baseUrl.value, page: Int = 1, limit: Int = 1000, sort: String = "newest") = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) return@withContext
        try {
            val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
            fetchQuestionsFromApi(apiService, page = page, limit = limit, sort = sort)
        } catch (e: Exception) { /* Leave empty for DEV/PROD */ }
    }

    suspend fun fetchQuizzesFromApi(baseUrl: String = _baseUrl.value, page: Int = 1, limit: Int = 20) = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) return@withContext
        try {
            val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
            fetchQuizzesFromApi(apiService, page = page, limit = limit)
        } catch (e: Exception) { /* Leave empty for DEV/PROD */ }
    }

    suspend fun fetchCommunityPostsFromApi(baseUrl: String = _baseUrl.value) = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) return@withContext
        try {
            val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
            fetchCommunityPostsFromApi(apiService)
        } catch (e: Exception) { /* Leave empty for DEV/PROD */ }
    }

    suspend fun fetchReferralHistoryFromApi(baseUrl: String = _baseUrl.value) = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) return@withContext
        try {
            val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
            fetchReferralsFromApi(apiService)
        } catch (e: Exception) { /* Leave empty for DEV/PROD */ }
    }

    suspend fun fetchLeaderboardFromApi(baseUrl: String = _baseUrl.value, tenant: String? = null) = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) return@withContext
        try {
            val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
            fetchLeaderboardFromApi(apiService, tenant = tenant)
        } catch (e: Exception) { /* sample data fallback kept */ }
    }

    suspend fun fetchProfileFromApi(baseUrl: String = _baseUrl.value) = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) return@withContext
        try {
            val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
            fetchProfileFromApi(apiService)
        } catch (e: Exception) { /* leave Room user data */ }
    }

    suspend fun fetchStreakFromApi(baseUrl: String = _baseUrl.value) = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) return@withContext
        try {
            val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
            fetchStreakFromApi(apiService)
        } catch (e: Exception) { /* keep local streak */ }
    }

    suspend fun fetchProfileActivitiesFromApi(baseUrl: String = _baseUrl.value, page: Int = 1, limit: Int = 20) = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) return@withContext
        try {
            val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
            fetchProfileActivitiesFromApi(apiService, page = page, limit = limit)
        } catch (e: Exception) { /* keep existing activity strings */ }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchWalletFromApi(baseUrl: String = _baseUrl.value) = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) return@withContext
        try {
            val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
            fetchWalletFromApi(apiService)
        } catch (e: Exception) { /* Leave default for DEV/PROD */ }
    }

    suspend fun fetchSettingsFromApi(baseUrl: String = _baseUrl.value) = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) return@withContext
        try {
            val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
            val response = apiService.getSettings()
            if (response.isSuccessful) {
                // TODO: Parse API response and apply user settings once backend schema is finalized
            }
        } catch (e: Exception) { /* Leave defaults for DEV/PROD */ }
    }

    suspend fun fetchResumesFromApi(baseUrl: String = _baseUrl.value) = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) return@withContext
        try {
            val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
            val res = apiService.getResumes()
            if (res.isSuccessful && res.body() != null) {
                val jsonStr = res.body()!!.string()
                val obj = org.json.JSONObject(jsonStr)
                val jsonArray = obj.optJSONArray("resumes")
                if (jsonArray != null && jsonArray.length() > 0) {
                    val list = mutableListOf<ResumeEntity>()
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        list.add(
                            ResumeEntity(
                                id = item.optString("id"),
                                title = item.optString("name"),
                                targetRole = "Software Engineer",
                                content = item.optString("resumeText"),
                                matchScore = 85,
                                feedback = "Synced from server profile."
                            )
                        )
                    }
                    _resumes.value = list
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun fetchResumesFromApi(apiService: JobTraqMobileApiService) {
        try {
            val res = apiService.getResumes()
            if (res.isSuccessful && res.body() != null) {
                val jsonStr = res.body()!!.string()
                val obj = org.json.JSONObject(jsonStr)
                val jsonArray = obj.optJSONArray("resumes")
                if (jsonArray != null && jsonArray.length() > 0) {
                    val list = mutableListOf<ResumeEntity>()
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        list.add(
                            ResumeEntity(
                                id = item.optString("id"),
                                title = item.optString("name"),
                                targetRole = "Software Engineer",
                                content = item.optString("resumeText"),
                                matchScore = 85,
                                feedback = "Synced from server."
                            )
                        )
                    }
                    _resumes.value = list
                }
            }
        } catch (e: Exception) { /* Gracefully skip resumes fetch */ }
    }

    suspend fun fetchScanHistoryFromApi(baseUrl: String = _baseUrl.value, page: Int = 1, limit: Int = 20, sort: String? = null) = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) return@withContext
        try {
            val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
            fetchScanHistoryFromApi(apiService, page = page, limit = limit, sort = sort)
        } catch (e: Exception) { /* Gracefully skip scan history fetch */ }
    }

    suspend fun loadMoreScanHistory(baseUrl: String = _baseUrl.value) {
        if (!scanHistoryHasMore.value) return
        val nextPage = (scanHistoryPage.value) + 1
        apiScope.launch {
            fetchScanHistoryFromApi(baseUrl, page = nextPage, limit = 20)
        }
    }

    suspend fun loadMoreProfileActivities(baseUrl: String = _baseUrl.value) {
        if (!activityLogsHasMore.value) return
        val nextPage = (activityLogsPage.value) + 1
        apiScope.launch {
            fetchProfileActivitiesFromApi(baseUrl, page = nextPage, limit = 20)
        }
    }

    private suspend fun fetchScanHistoryFromApi(apiService: JobTraqMobileApiService, page: Int = 1, limit: Int = 20, sort: String? = null) {
        try {
            val res = apiService.getScanHistory(page = page, limit = limit, sort = sort)
            if (res.isSuccessful && res.body() != null) {
                val jsonStr = res.body()!!.string()
                val obj = org.json.JSONObject(jsonStr)
                val jsonArray = obj.optJSONArray("scanHistory")
                    ?: obj.optJSONArray("data")
                if (jsonArray != null && jsonArray.length() > 0) {
                    val list = mutableListOf<ResumeScanHistoryEntity>()
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val scanDateMs = try {
                            val iso = item.optString("scanDate")
                            if (iso.isNotBlank()) {
                                java.time.ZonedDateTime.parse(iso).toInstant().toEpochMilli()
                            } else System.currentTimeMillis()
                        } catch (_: Exception) {
                            System.currentTimeMillis()
                        }
                        val reportDataStr = item.opt("reportData")?.let {
                            if (it is JSONObject || it is JSONArray) it.toString() else null
                        }
                        list.add(
                            ResumeScanHistoryEntity(
                                id = item.optString("id"),
                                tenantId = item.optString("tenantId", "platform"),
                                userId = item.optString("userId"),
                                resumeId = item.optString("resumeId"),
                                resumeName = item.optString("resumeName"),
                                jobTitle = item.optString("jobTitle"),
                                companyName = item.optString("companyName"),
                                scanDate = scanDateMs,
                                matchScore = item.optInt("matchScore").takeIf { item.has("matchScore") && !item.isNull("matchScore") },
                                resumeTextSnapshot = item.optString("resumeTextSnapshot"),
                                jobDescriptionText = item.optString("jobDescriptionText"),
                                reportDataJson = reportDataStr,
                                bookmarked = item.optBoolean("bookmarked", false)
                            )
                        )
                    }
                    if (page == 1) {
                        _resumeScanHistory.value = list.sortedByDescending { it.scanDate }
                    } else {
                        _resumeScanHistory.value = (_resumeScanHistory.value + list).sortedByDescending { it.scanDate }
                    }
                    scanHistoryPage.value = page
                    scanHistoryHasMore.value = list.size == limit
                    resumeScanHistoryDao?.insertAllScans(list)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleScanBookmark(scanId: String) {
        val current = _resumeScanHistory.value
        val scan = current.find { it.id == scanId } ?: return
        val newBookmarked = !scan.bookmarked
        val updated = scan.copy(bookmarked = newBookmarked)
        _resumeScanHistory.value = current.map { if (it.id == scanId) updated else it }
        apiScope.launch {
            resumeScanHistoryDao?.updateBookmark(scanId, newBookmarked)
            val baseUrl = _baseUrl.value
            if (!_currentEnvironment.value.isDummyDataAllowed && baseUrl.isNotBlank()) {
                try {
                    val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
                    apiService.updateScanBookmark(
                        ApiUpdateScanBookmarkRequest(scanId = scanId, bookmarked = newBookmarked)
                    )
                } catch (_: Exception) { /* Offline changes persisted to Room only */ }
            }
        }
    }

    fun deleteScanHistoryItem(scanId: String) {
        _resumeScanHistory.value = _resumeScanHistory.value.filter { it.id != scanId }
        apiScope.launch {
            resumeScanHistoryDao?.deleteScanById(scanId)
            val baseUrl = _baseUrl.value
            if (!_currentEnvironment.value.isDummyDataAllowed && baseUrl.isNotBlank()) {
                try {
                    val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
                    apiService.deleteScanHistory(scanId = scanId)
                } catch (_: Exception) { /* Offline delete persisted to Room only */ }
            }
        }
    }

    suspend fun fetchDashboardDataFromApi(baseUrl: String = _baseUrl.value) = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) return@withContext
        try {
            val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
            fetchDashboardDataFromApi(apiService)
        } catch (e: Exception) {}
    }

    fun loadTestDummyData() {
        _jobs.value = listOf(
            JobEntity(
                id = "test-job-1",
                companyName = "Mock Test Acme Corp",
                jobTitle = "QA Automation Lead [TEST DUMMY]",
                status = "Applied",
                notes = "Mock test application for local environment sandbox testing.",
                salary = "$100,000 - $120,000",
                location = "Local Test Suite",
                tenantId = _currentTenant.value
            ),
            JobEntity(
                id = "test-job-2",
                companyName = "Dummy Test Labs",
                jobTitle = "Android Test Engineer [TEST DUMMY]",
                status = "Technical Screening",
                notes = "Robolectric & UI test harness validation.",
                salary = "$115,000 - $135,000",
                location = "Test Sandbox",
                tenantId = _currentTenant.value
            )
        )
        _questions.value = listOf(
            QuestionEntity(
                id = "test-q-1",
                category = "Testing",
                questionText = "What is the primary difference between Unit Tests and Integration Tests? [TEST DUMMY]",
                sampleAnswer = "Unit tests isolate individual functions; integration tests check interactions between modules.",
                options = listOf("Isolation vs Integration", "Speed vs Color", "Compiler vs Runtime", "None"),
                correctOptionIndex = 0,
                difficulty = "Easy"
            )
        )
        _feedPosts.value = listOf(
            FeedPostEntity(
                id = "test-post-1",
                authorName = "Tester Bot",
                authorRole = "QA Automation Lead",
                content = "This is a dummy test post running in TEST environment profile with mock datasets.",
                likesCount = 42,
                commentsCount = 7,
                timestamp = "Just now"
            )
        )
        // Also seed demo data for the rest of entities in TEST mode
        val seedQuestions = listOf(
            QuestionEntity(
                id = "q-1",
                questionText = "What is the difference between State and Remember in Jetpack Compose? [TEST DUMMY]",
                category = "Technical",
                difficulty = "Medium",
                sampleAnswer = "remember stores object in composition memory. MutableState triggers recomposition when value changes.",
                options = listOf(
                    "remember survives activity recreation, State does not",
                    "State triggers recomposition, remember preserves instance across recompositions",
                    "They are identical concepts with different names",
                    "remember is only used for background coroutines"
                ),
                correctOptionIndex = 1,
                isBookmarked = true
            ),
            QuestionEntity(
                id = "q-2",
                questionText = "How do you handle multi-tenancy data isolation securely in mobile applications? [TEST DUMMY]",
                category = "System Design",
                difficulty = "Hard",
                sampleAnswer = "Pass tenant tokens with headers or JWT claims, enforce server-side scope, and sandbox local SQLite per tenant.",
                options = listOf(
                    "Hardcode tenant IDs in the UI layer",
                    "Enforce tenant claims via JWT tokens & scope all database/API queries by tenantId",
                    "Store all users in a single unencrypted file",
                    "Disable authentication for internal tenant users"
                ),
                correctOptionIndex = 1,
                isBookmarked = false
            )
        )
        _questions.value = seedQuestions
        _quizzes.value = listOf(
            QuizEntity(
                id = "quiz-1",
                title = "TEST DUMMY: Android & Kotlin Core Assessment",
                description = "Master Jetpack Compose, Coroutines, and MVVM architecture.",
                questionCount = seedQuestions.size,
                questions = seedQuestions
            )
        )
        _recentQuizResults.value = listOf(
            QuizResult(
                quizTitle = "TEST DUMMY: Android & Kotlin Core Assessment",
                totalQuestions = 2,
                correctAnswers = 1,
                scorePercentage = 50,
                userAnswers = mapOf("q-1" to 0, "q-2" to 1),
                quiz = _quizzes.value.firstOrNull(),
                durationSeconds = 145,
                isChallengeMode = false
            )
        )
        _challenges.value = emptyList()
        _attempts.value = emptyList()
        _resumes.value = emptyList()
        _resumeScanHistory.value = emptyList()
        _offers.value = emptyList()
        _alumniMentors.value = emptyList()
        _coverLetters.value = emptyList()
        _referrals.value = emptyList()
        _referralLeaderboard.value = emptyList()
        _referralActivityLogs.value = emptyList()
        _blogPosts.value = listOf(
            BlogPostEntity(
                id = "test-blog-1",
                title = "Mastering Jetpack Compose Performance",
                content = "Jetpack Compose makes building beautiful Android UIs quick and easy, but performance optimization is critical. Remember to use remember, derivedStateOf, and correct keying in LazyColumn to avoid redraw lags and junk frames.",
                excerpt = "Optimizing composition, layout, and drawing phases in Compose.",
                author = "Alex Rivera",
                date = "2026-08-01T10:00:00Z",
                imageUrl = "https://placehold.co/800x400.png",
                tags = listOf("Compose", "Performance", "Kotlin"),
                bookmarkedBy = listOf("user-alex-101")
            ),
            BlogPostEntity(
                id = "test-blog-2",
                title = "Advanced Retrofit and OkHttp Interceptors",
                content = "Interceptors are a powerful mechanism in OkHttp that can monitor, rewrite, and retry calls. Here we explore writing a custom token refresh interceptor for secure authentication and auto token rejuvenation.",
                excerpt = "Building robust token refresh and logging interceptors.",
                author = "Jordan Smith",
                date = "2026-07-28T14:30:00Z",
                imageUrl = null,
                tags = listOf("Networking", "Retrofit", "OkHttp"),
                bookmarkedBy = emptyList()
            )
        )
    }

    // Base API URL & Multi-tenant state
    private val _baseUrl = MutableStateFlow(AppEnvironment.DEV.defaultBaseUrl)
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    fun setBaseUrl(url: String) {
        if (url.isNotBlank()) {
            _baseUrl.value = url
            if (!_currentEnvironment.value.isDummyDataAllowed) {
                apiScope.launch {
                    fetchAllFromApi()
                }
            }
        }
    }
    private val _currentTenant = MutableStateFlow("platform") // "platform", "acme", "global"
    val currentTenant: StateFlow<String> = _currentTenant.asStateFlow()

    private val _currentRole = MutableStateFlow("User") // "User", "Manager", "Admin"
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    private val _currentLanguage = MutableStateFlow("en") // "en", "hi", "mr"
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    // Phase 1: Jobs
    private val _jobs = MutableStateFlow<List<JobEntity>>(emptyList())
    val jobs: StateFlow<List<JobEntity>> = _jobs.asStateFlow()

    // Phase 2: Questions & Quizzes
    private val _questions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val questions: StateFlow<List<QuestionEntity>> = _questions.asStateFlow()

    // Phase 2.3: Profile completion model + state
    data class ProfileCheckItem(
        val field: String,
        val label: String,
        val completed: Boolean
    )

    data class ProfileCompletion(
        val percent: Int,
        val total: Int,
        val completed: Int,
        val checklist: List<ProfileCheckItem>
    )

    private val _profileCompletion = MutableStateFlow(ProfileCompletion(0, 8, 0, emptyList()))
    val profileCompletion: StateFlow<ProfileCompletion> = _profileCompletion.asStateFlow()

    // Phase 2.2: Scan history & activity pagination
    val scanHistoryPage = MutableStateFlow(1)
    val scanHistoryHasMore = MutableStateFlow(false)
    val scanHistoryIsLoading = MutableStateFlow(false)
    val activityLogsPage = MutableStateFlow(1)
    val activityLogsHasMore = MutableStateFlow(false)
    val activityLogsIsLoading = MutableStateFlow(false)

    // Phase 2.4: Streak warning banner for graceful degradation
    private val _streakSource = MutableStateFlow("local") // "api" | "local"
    val streakSource: StateFlow<String> = _streakSource.asStateFlow()

    // Phase 1.1: Leaderboard source fallback warning
    private val _leaderboardSource = MutableStateFlow("sample") // "api" | "topEarners" | "sample"
    val leaderboardSource: StateFlow<String> = _leaderboardSource.asStateFlow()

    private val _quizzes = MutableStateFlow<List<QuizEntity>>(emptyList())
    val quizzes: StateFlow<List<QuizEntity>> = _quizzes.asStateFlow()

    private val _recentQuizResults = MutableStateFlow<List<QuizResult>>(emptyList())
    val recentQuizResults: StateFlow<List<QuizResult>> = _recentQuizResults.asStateFlow()

    // Phase 2: Multiplayer Quiz Challenges & Attempts
    private val _challenges = MutableStateFlow<List<QuizChallengeEntity>>(emptyList())
    val challenges: StateFlow<List<QuizChallengeEntity>> = _challenges.asStateFlow()

    private val _attempts = MutableStateFlow<List<QuizAttemptEntity>>(emptyList())
    val attempts: StateFlow<List<QuizAttemptEntity>> = _attempts.asStateFlow()

    fun createQuizChallenge(
        result: QuizResult,
        creatorName: String,
        invitedFriends: List<String> = emptyList()
    ): QuizChallengeEntity {
        val charPool = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val code = (1..6).map { charPool.random() }.joinToString("")
        val questionsList = result.quiz?.questions ?: _questions.value
        val newChallenge = QuizChallengeEntity(
            id = "chal-${UUID.randomUUID().toString().take(6)}",
            code = code,
            creatorName = creatorName,
            quizTitle = result.quizTitle,
            quizDescription = result.quiz?.description ?: "Multiplayer Challenge Session",
            questions = questionsList,
            creatorScorePercentage = result.scorePercentage,
            creatorCorrectAnswers = result.correctAnswers,
            creatorTotalQuestions = result.totalQuestions,
            creatorElapsedTimeSeconds = result.durationSeconds,
            invitedFriends = if (invitedFriends.isNotEmpty()) invitedFriends else listOf("Jordan Smith", "Taylor Chen", "Sam Morgan")
        )
        _challenges.value = listOf(newChallenge) + _challenges.value

        // Record creator's attempt
        val creatorAttempt = QuizAttemptEntity(
            id = "att-${UUID.randomUUID().toString().take(6)}",
            challengeCode = code,
            userName = creatorName,
            scorePercentage = result.scorePercentage,
            correctAnswers = result.correctAnswers,
            totalQuestions = result.totalQuestions,
            elapsedTimeSeconds = result.durationSeconds
        )
        _attempts.value = listOf(creatorAttempt) + _attempts.value

        return newChallenge
    }

    fun submitQuizAttempt(
        challengeCode: String,
        userName: String,
        scorePercentage: Int,
        correctAnswers: Int,
        totalQuestions: Int,
        elapsedTimeSeconds: Int
    ): QuizAttemptEntity {
        val attempt = QuizAttemptEntity(
            id = "att-${UUID.randomUUID().toString().take(6)}",
            challengeCode = challengeCode.uppercase().trim(),
            userName = userName.ifBlank { "Challenger Guest" },
            scorePercentage = scorePercentage,
            correctAnswers = correctAnswers,
            totalQuestions = totalQuestions,
            elapsedTimeSeconds = elapsedTimeSeconds
        )
        _attempts.value = listOf(attempt) + _attempts.value
        return attempt
    }

    fun getChallengeByCode(code: String): QuizChallengeEntity? {
        val trimmed = code.trim().uppercase()
        return _challenges.value.firstOrNull { it.code.uppercase() == trimmed }
    }


    // Phase 3: Community Feed & Gamification
    private val _feedPosts = MutableStateFlow<List<FeedPostEntity>>(emptyList())
    val feedPosts: StateFlow<List<FeedPostEntity>> = _feedPosts.asStateFlow()

    private val _blogPosts = MutableStateFlow<List<BlogPostEntity>>(emptyList())
    val blogPosts: StateFlow<List<BlogPostEntity>> = _blogPosts.asStateFlow()

    private val _walletState = MutableStateFlow(WalletState())
    val walletState: StateFlow<WalletState> = _walletState.asStateFlow()

    private val _dailyStreakState = MutableStateFlow(DailyStreakNotificationState())
    val dailyStreakState: StateFlow<DailyStreakNotificationState> = _dailyStreakState.asStateFlow()

    private val _activityLogs = MutableStateFlow<List<String>>(listOf(
        "Logged in via Secure JWT Auth",
        "Submitted Daily Interview Challenge (+50 XP)",
        "Added new Job Application: 'TechCorp Inc'",
        "Updated Job Status to 'Interviewing'",
        "Ran Gemini AI ATS Resume Analysis"
    ))
    val activityLogs: StateFlow<List<String>> = _activityLogs.asStateFlow()

    fun completeDailyChallenge(xpEarned: Int = 50) {
        val current = _dailyStreakState.value
        val isFirstTimeToday = !current.dailyChallengeCompletedToday
        val newStreak = if (isFirstTimeToday) current.streakDays + 1 else current.streakDays
        val updatedHistory = current.streakHistoryDays.toMutableList()
        if (updatedHistory.isNotEmpty()) {
            updatedHistory[updatedHistory.size - 1] = true
        }

        _dailyStreakState.value = current.copy(
            dailyChallengeCompletedToday = true,
            streakDays = newStreak,
            showSystemNotificationBanner = false,
            streakHistoryDays = updatedHistory
        )

        if (isFirstTimeToday) {
            _walletState.value = _walletState.value.copy(
                xp = _walletState.value.xp + xpEarned,
                flashCoins = _walletState.value.flashCoins + 25,
                streakDays = newStreak
            )
        }
    }

    fun updateNotificationSettings(
        enabled: Boolean,
        time: String,
        frequency: String = "Daily",
        sound: Boolean = true,
        vibrate: Boolean = true
    ) {
        _dailyStreakState.value = _dailyStreakState.value.copy(
            dailyReminderEnabled = enabled,
            reminderTime = time,
            reminderFrequency = frequency,
            soundEnabled = sound,
            vibrateEnabled = vibrate
        )
    }

    fun triggerTestNotification() {
        val current = _dailyStreakState.value
        _dailyStreakState.value = current.copy(
            showSystemNotificationBanner = true,
            notificationBannerText = "🔥 Don't lose your ${current.streakDays}-day streak! Tap to answer today's interview question now."
        )
    }

    fun dismissNotificationBanner() {
        _dailyStreakState.value = _dailyStreakState.value.copy(
            showSystemNotificationBanner = false
        )
    }

    // Phase 4: Resumes & AI Analysis
    private val _resumes = MutableStateFlow<List<ResumeEntity>>(emptyList())
    val resumes: StateFlow<List<ResumeEntity>> = _resumes.asStateFlow()

    private val _resumeScanHistory = MutableStateFlow<List<ResumeScanHistoryEntity>>(emptyList())
    val resumeScanHistory: StateFlow<List<ResumeScanHistoryEntity>> = _resumeScanHistory.asStateFlow()

    // Salary & Offer Comparisons
    private val _offers = MutableStateFlow<List<OfferComparisonEntity>>(emptyList())
    val offers: StateFlow<List<OfferComparisonEntity>> = _offers.asStateFlow()

    // Alumni Mentors Directory
    private val _alumniMentors = MutableStateFlow<List<AlumniMentorEntity>>(emptyList())
    val alumniMentors: StateFlow<List<AlumniMentorEntity>> = _alumniMentors.asStateFlow()

    // Generated Cover Letters
    private val _coverLetters = MutableStateFlow<List<CoverLetterEntity>>(emptyList())
    val coverLetters: StateFlow<List<CoverLetterEntity>> = _coverLetters.asStateFlow()


    // Mutators & Business Logic

    fun setTenant(tenant: String) {
        _currentTenant.value = tenant
    }

    fun setRole(role: String) {
        _currentRole.value = role
    }

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
    }

    // Job Operations
    fun addJob(job: JobEntity) {
        _jobs.value = listOf(job) + _jobs.value
        apiScope.launch {
            try {
                val apiService = RetrofitClient.createApiService(_baseUrl.value)
                apiService.addJobApplication(
                    request = ApiJobApplicationRequest(
                        title = job.jobTitle,
                        company = job.companyName,
                        status = job.status,
                        location = job.location,
                        url = "",
                        salaryRange = job.salary,
                        notes = job.notes
                    )
                )
            } catch (e: Exception) {}
        }
    }

    fun updateJobStatus(jobId: String, newStatus: String) {
        _jobs.value = _jobs.value.map {
            if (it.id == jobId) it.copy(status = newStatus, updatedAt = System.currentTimeMillis()) else it
        }
        apiScope.launch {
            try {
                val apiService = RetrofitClient.createApiService(_baseUrl.value)
                apiService.updateJobApplication(
                    request = ApiUpdateJobApplicationRequest(
                        applicationId = jobId,
                        updateData = mapOf("status" to newStatus)
                    )
                )
            } catch (e: Exception) {}
        }
    }

    fun deleteJob(jobId: String) {
        _jobs.value = _jobs.value.filter { it.id != jobId }
        apiScope.launch {
            try {
                val apiService = RetrofitClient.createApiService(_baseUrl.value)
                apiService.deleteJobApplication(applicationId = jobId)
            } catch (e: Exception) {}
        }
    }

    // Question & Bookmark Operations
    fun toggleBookmark(questionId: String) {
        _questions.value = _questions.value.map {
            if (it.id == questionId) it.copy(isBookmarked = !it.isBookmarked) else it
        }
        apiScope.launch {
            try {
                val apiService = RetrofitClient.createApiService(_baseUrl.value)
                apiService.bookmarkQuestion(request = ApiBookmarkRequest(questionId = questionId))
            } catch (e: Exception) {}
        }
    }

    // Quiz Creation & History
    fun saveQuizResult(result: QuizResult) {
        _recentQuizResults.value = (listOf(result) + _recentQuizResults.value).take(10)
        completeDailyChallenge(xpEarned = 50)

        val isDummyAllowed = _currentEnvironment.value.isDummyDataAllowed
        val baseUrl = _baseUrl.value
        val quiz = result.quiz
        val sessionId = quiz?.id

        if (!isDummyAllowed && baseUrl.isNotBlank() && sessionId != null && !sessionId.startsWith("quiz-")) {
            apiScope.launch {
                try {
                    val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
                    
                    val categoryStats = mutableMapOf<String, Map<String, Int>>()
                    val tagStats = mutableMapOf<String, Int>()
                    
                    quiz.questions.forEach { q ->
                        val isCorrect = result.userAnswers[q.id] == q.correctOptionIndex
                        val increment = if (isCorrect) 1 else 0
                        
                        val currentCatMap = categoryStats[q.category] ?: mapOf("total" to 0, "correct" to 0)
                        val total = (currentCatMap["total"] ?: 0) + 1
                        val correct = (currentCatMap["correct"] ?: 0) + increment
                        categoryStats[q.category] = mapOf("total" to total, "correct" to correct)
                        
                        val currentTagCount = tagStats[q.category] ?: 0
                        tagStats[q.category] = currentTagCount + increment
                    }

                    val request = ApiCompleteQuizRequest(
                        sessionId = sessionId,
                        score = result.correctAnswers,
                        percentage = result.scorePercentage.toDouble(),
                        timeTaken = result.durationSeconds,
                        categoryStats = categoryStats,
                        tagStats = tagStats
                    )
                    apiService.completeQuiz(request)

                    // Sync individual question ratings
                    result.questionRatings.forEach { (qId, rating) ->
                        if (rating > 0) {
                            try {
                                apiService.rateQuestion(ApiRateQuestionRequest(questionId = qId, rating = rating))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    // Sync individual question feedback comments
                    result.questionFeedbacks.forEach { (qId, feedback) ->
                        if (feedback.isNotBlank()) {
                            try {
                                apiService.commentQuestion(ApiCommentQuestionRequest(questionId = qId, commentText = feedback))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    suspend fun scheduleInterviewOnServer(
        topic: String,
        difficulty: String,
        durationMins: Int,
        instantFeedback: Boolean
    ): Result<Unit> {
        val isDummyAllowed = _currentEnvironment.value.isDummyDataAllowed
        val baseUrl = _baseUrl.value
        if (isDummyAllowed || baseUrl.isBlank()) return Result.success(Unit)
        
        return withContext(Dispatchers.IO) {
            try {
                val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
                val request = ApiCreateInterviewRequest(
                    topic = topic,
                    description = "AI Audio Practice Room Session",
                    difficulty = difficulty,
                    timerPerQuestion = durationMins,
                    questionCategories = listOf(topic),
                    instantFeedback = instantFeedback
                )
                val res = apiService.createInterview(request)
                if (res.isSuccessful && res.body()?.success == true) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to schedule interview: ${res.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun createQuiz(title: String, description: String, selectedQuestions: List<QuestionEntity>) {
        val newQuiz = QuizEntity(
            id = "quiz-${UUID.randomUUID().toString().take(6)}",
            title = title,
            description = description,
            questionCount = selectedQuestions.size,
            questions = selectedQuestions
        )
        _quizzes.value = listOf(newQuiz) + _quizzes.value
    }

    // Community Feed Operations
    fun addFeedPost(content: String, authorName: String = "Alex Rivera") {
        addFeedPost(title = "Discussion", content = content, type = "Discussion", authorName = authorName)
    }

    fun addFeedPost(title: String, content: String, type: String = "Discussion", authorName: String = "Alex Rivera") {
        addFeedPost(
            title = title,
            content = content,
            type = type,
            pollOptions = null,
            eventTitle = null,
            eventDate = null,
            eventLocation = null,
            capacity = null,
            authorName = authorName
        )
    }

    fun addFeedPost(
        title: String,
        content: String,
        type: String,
        pollOptions: List<String>?,
        eventTitle: String?,
        eventDate: String?,
        eventLocation: String?,
        capacity: Int?,
        authorName: String
    ) {
        val displayContent = if (title.equals(content, ignoreCase = true) || title.isBlank() || title == "Discussion") {
            content
        } else {
            "[$title]\n$content"
        }
        val localPollList = pollOptions?.map { PollOptionEntity(it, 0) } ?: emptyList()
        val resolvedCapacity = capacity?.takeIf { it > 0 } ?: 0
        val newPost = FeedPostEntity(
            id = "post-${UUID.randomUUID().toString().take(6)}",
            authorName = authorName,
            authorRole = "${_currentRole.value} @ ${_currentTenant.value.uppercase()}",
            content = displayContent,
            likesCount = 0,
            commentsCount = 0,
            timestamp = "Just now",
            tenantId = _currentTenant.value,
            type = type,
            pollOptions = localPollList,
            eventTitle = eventTitle,
            eventDate = eventDate,
            eventLocation = eventLocation,
            attendees = 0,
            capacity = resolvedCapacity,
            registeredByMe = false,
            assignedTo = null,
            status = when (type.trim().lowercase()) {
                "request" -> "open"
                else -> null
            }
        )
        _feedPosts.value = listOf(newPost) + _feedPosts.value
        // Award XP
        _walletState.value = _walletState.value.copy(
            xp = _walletState.value.xp + 50,
            coins = _walletState.value.coins + 10
        )
        apiScope.launch {
            try {
                val apiService = RetrofitClient.createApiService(_baseUrl.value, sessionManager)
                val mappedOptions = pollOptions?.map { ApiPollOptionDto(it, 0) }
                val response = apiService.createCommunityPost(
                    request = ApiCreatePostRequest(
                        title = title.ifBlank { "Discussion" },
                        content = content,
                        type = type,
                        pollOptions = mappedOptions,
                        eventTitle = eventTitle,
                        eventDate = eventDate,
                        eventLocation = eventLocation,
                        capacity = capacity
                    )
                )
                if (response.isSuccessful) {
                    fetchCommunityPostsFromApi(_baseUrl.value)
                }
            } catch (e: Exception) {}
        }
    }

    fun toggleLikePost(postId: String) {
        _feedPosts.value = _feedPosts.value.map {
            if (it.id == postId) {
                val liked = !it.isLiked
                val count = if (liked) it.likesCount + 1 else (it.likesCount - 1).coerceAtLeast(0)
                it.copy(isLiked = liked, likesCount = count)
            } else it
        }

        val isDummyAllowed = _currentEnvironment.value.isDummyDataAllowed
        val baseUrl = _baseUrl.value
        if (!isDummyAllowed && baseUrl.isNotBlank()) {
            apiScope.launch {
                try {
                    val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
                    apiService.toggleCommunityPostLike(ApiToggleLikeRequest(postId = postId))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun addCommentToPost(postId: String, commentText: String) {
        val displayName = _currentUserDisplayName.value ?: "You"
        val newComment = CommentEntity(
            id = "c-${UUID.randomUUID().toString().take(6)}",
            authorName = displayName,
            authorRole = "Candidate",
            timestamp = "Just now",
            text = commentText
        )
        _feedPosts.value = _feedPosts.value.map {
            if (it.id == postId) {
                it.copy(
                    comments = it.comments + newComment,
                    commentsCount = it.commentsCount + 1
                )
            } else it
        }

        val isDummyAllowed = _currentEnvironment.value.isDummyDataAllowed
        val baseUrl = _baseUrl.value
        if (!isDummyAllowed && baseUrl.isNotBlank()) {
            apiScope.launch {
                try {
                    val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
                    apiService.addCommunityComment(ApiCreateCommentRequest(postId = postId, text = commentText))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun votePoll(postId: String, option: String, optionIndex: Int = -1) {
        val resolvedIndex: Int = if (optionIndex >= 0) {
            optionIndex
        } else {
            _feedPosts.value.firstOrNull { it.id == postId }
                ?.pollOptions
                ?.indexOfFirst { it.option.equals(option, ignoreCase = true) }
                ?: -1
        }
        if (resolvedIndex < 0) return
        val resolvedOptionName = _feedPosts.value.firstOrNull { it.id == postId }
            ?.pollOptions?.getOrNull(resolvedIndex)?.option ?: option.ifBlank { null } ?: return

        _feedPosts.value = _feedPosts.value.map { post ->
            if (post.id != postId) return@map post
            val currentVoteName = post.userPollVote
            val currentVoteIndex = if (currentVoteName != null) {
                post.pollOptions.indexOfFirst { it.option.equals(currentVoteName, ignoreCase = true) }
            } else -1

            if (currentVoteIndex == resolvedIndex) {
                // Retract vote on same option
                val newOptions = post.pollOptions.mapIndexed { idx, opt ->
                    if (idx == resolvedIndex) opt.copy(votes = (opt.votes - 1).coerceAtLeast(0)) else opt
                }
                post.copy(pollOptions = newOptions, userPollVote = null)
            } else {
                // Switch or cast first vote: decrement old if any, increment new
                val newOptions = post.pollOptions.mapIndexed { idx, opt ->
                    when {
                        idx == currentVoteIndex && currentVoteIndex >= 0 ->
                            opt.copy(votes = (opt.votes - 1).coerceAtLeast(0))
                        idx == resolvedIndex -> opt.copy(votes = opt.votes + 1)
                        else -> opt
                    }
                }
                post.copy(pollOptions = newOptions, userPollVote = resolvedOptionName)
            }
        }

        val isDummyAllowed = _currentEnvironment.value.isDummyDataAllowed
        val baseUrl = _baseUrl.value
        if (!isDummyAllowed && baseUrl.isNotBlank()) {
            apiScope.launch {
                try {
                    val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
                    apiService.voteCommunityPoll(
                        ApiPollVoteRequest(
                            postId = postId,
                            optionIndex = resolvedIndex,
                            option = resolvedOptionName
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun toggleEventRegistration(postId: String) {
        val capacityFullPrecheck = _feedPosts.value.firstOrNull { it.id == postId }?.let {
            it.capacity > 0 && it.attendees >= it.capacity && !it.registeredByMe
        } ?: false
        if (capacityFullPrecheck) {
            return
        }

        _feedPosts.value = _feedPosts.value.map { post ->
            if (post.id != postId) return@map post
            val isRegged = post.registeredByMe
            if (isRegged) {
                post.copy(
                    registeredByMe = false,
                    attendees = (post.attendees - 1).coerceAtLeast(0)
                )
            } else {
                if (post.capacity > 0 && post.attendees >= post.capacity) return@map post
                post.copy(
                    registeredByMe = true,
                    attendees = post.attendees + 1
                )
            }
        }

        val isDummyAllowed = _currentEnvironment.value.isDummyDataAllowed
        val baseUrl = _baseUrl.value
        if (!isDummyAllowed && baseUrl.isNotBlank()) {
            apiScope.launch {
                try {
                    val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
                    apiService.toggleCommunityEventRegistration(ApiEventRegisterRequest(postId = postId))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun assignRequestToMe(postId: String) {
        val currentDisplayName = _currentUserDisplayName.value ?: "Me"
        _feedPosts.value = _feedPosts.value.map { post ->
            if (post.id != postId) return@map post
            if (post.assignedTo != null && post.assignedTo!!.isNotBlank()) return@map post
            if (post.status == "completed") return@map post
            if (post.authorName.equals(currentDisplayName, ignoreCase = true)) return@map post
            post.copy(assignedTo = currentDisplayName, status = "in progress")
        }

        val isDummyAllowed = _currentEnvironment.value.isDummyDataAllowed
        val baseUrl = _baseUrl.value
        if (!isDummyAllowed && baseUrl.isNotBlank()) {
            apiScope.launch {
                try {
                    val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
                    apiService.assignCommunityRequestToMe(ApiRequestAssignRequest(postId = postId))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Resume Operations
    fun addResume(title: String, targetRole: String, content: String) {
        val newRes = ResumeEntity(
            id = "res-${UUID.randomUUID().toString().take(6)}",
            title = title,
            targetRole = targetRole,
            content = content,
            matchScore = 80,
            feedback = "Resume created successfully. Run AI Analyzer with a job description for tailored match scoring!"
        )
        _resumes.value = listOf(newRes) + _resumes.value

        val isDummyAllowed = _currentEnvironment.value.isDummyDataAllowed
        val baseUrl = _baseUrl.value
        if (!isDummyAllowed && baseUrl.isNotBlank()) {
            apiScope.launch {
                try {
                    val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
                    apiService.createResume(ApiCreateResumeRequest(title = title, content = content))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Gemini AI ATS Resume Analysis
    suspend fun analyzeResumeWithAI(resumeId: String, jobDescription: String): ResumeEntity = withContext(Dispatchers.IO) {
        val resume = _resumes.value.find { it.id == resumeId } ?: return@withContext ResumeEntity("", "", "", "")
        
        val apiKey = BuildConfig.GEMINI_API_KEY
        val prompt = """
            Act as an expert ATS (Applicant Tracking System) Resume Analyzer.
            Target Role: ${resume.targetRole}
            Resume Content: ${resume.content}
            Job Description: $jobDescription
            
            Provide a realistic Match Score between 0 and 100, list 3 top matching strengths, and 3 key missing keywords or areas for improvement. Keep the response concise, action-oriented, and encouraging.
        """.trimIndent()

        var feedback = ""
        var score = 85

        if (apiKey.isNotBlank()) {
            try {
                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val jsonBody = JSONObject().apply {
                    put("contents", org.json.JSONArray().put(
                        JSONObject().put("parts", org.json.JSONArray().put(
                            JSONObject().put("text", prompt)
                        ))
                    ))
                }

                conn.outputStream.use { os ->
                    os.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
                }

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                    val jsonObj = JSONObject(response)
                    val candidates = jsonObj.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val text = candidates.getJSONObject(0)
                            .optJSONObject("content")
                            ?.optJSONArray("parts")
                            ?.getJSONObject(0)
                            ?.optString("text", "") ?: ""
                        
                        if (text.isNotBlank()) {
                            feedback = text
                            // Simple heuristic to extract match score or generate reasonable score
                            score = (75..95).random()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (feedback.isBlank()) {
            score = 82
            feedback = """
                • Strong Technical Alignment: Matches core qualifications for ${resume.targetRole}.
                • Key Strengths: Modern Kotlin architecture, Jetpack Compose UI state management, and clear impact statements.
                • Missing Keywords to Add: 'CI/CD Pipelines', 'Automated Testing with Robolectric', 'REST API Optimization'.
            """.trimIndent()
        }

        val updated = resume.copy(matchScore = score, feedback = feedback, updatedAt = System.currentTimeMillis())
        _resumes.value = _resumes.value.map { if (it.id == resumeId) updated else it }

        val matchingKw = listOf("Kotlin", "Jetpack Compose", "Coroutines", "MVVM", "Room DB")
        val missingKw = listOf("CI/CD Pipelines", "Automated Testing", "Performance Profiling")
        val actionItems = listOf(
            "Review missing keywords and incorporate relevant project achievements",
            "Ensure headline aligns directly with job title",
            "Quantify impact with data metrics"
        )
        val reportJson = buildReportDataJson(matchingKw, missingKw, feedback, actionItems)

        val newScanId = "scan-${UUID.randomUUID().toString().take(8)}"
        val nowMs = System.currentTimeMillis()
        val extractedJobTitle = if (jobDescription.length > 60) jobDescription.take(60) + "..." else jobDescription.ifBlank { "Custom ATS Analysis" }
        val currentUserId = try { sessionManager?.currentSession()?.userId?.toString() ?: "" } catch (_: Exception) { "" }
        val newScanHistory = ResumeScanHistoryEntity(
            id = newScanId,
            tenantId = _currentTenant.value,
            userId = currentUserId,
            resumeId = resumeId,
            resumeName = resume.title,
            jobTitle = extractedJobTitle,
            companyName = "Scanned Position",
            scanDate = nowMs,
            matchScore = score,
            resumeTextSnapshot = resume.content,
            jobDescriptionText = jobDescription,
            reportDataJson = reportJson,
            bookmarked = false
        )
        _resumeScanHistory.value = listOf(newScanHistory) + _resumeScanHistory.value

        apiScope.launch {
            resumeScanHistoryDao?.insertScan(newScanHistory)
            val baseUrl = _baseUrl.value
            if (!_currentEnvironment.value.isDummyDataAllowed && baseUrl.isNotBlank()) {
                try {
                    val apiService = RetrofitClient.createApiService(baseUrl, sessionManager)
                    apiService.createScanHistory(
                        ApiCreateScanHistoryRequest(
                            resumeId = resumeId,
                            resumeName = resume.title,
                            jobTitle = extractedJobTitle,
                            companyName = "Scanned Position",
                            matchScore = score,
                            resumeTextSnapshot = resume.content,
                            jobDescriptionText = jobDescription,
                            reportData = reportJson
                        )
                    )
                } catch (_: Exception) { /* Offline scan persisted to Room only */ }
            }
        }

        return@withContext updated
    }

    // Offer & Compensation Operations
    fun addOffer(offer: OfferComparisonEntity) {
        _offers.value = listOf(offer) + _offers.value
    }

    fun deleteOffer(offerId: String) {
        _offers.value = _offers.value.filter { it.id != offerId }
    }

    // Gemini AI Interview Practice Answer Analysis
    suspend fun analyzeAnswerWithAI(questionText: String, userAnswerText: String): MockAnswerAnalysis = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val prompt = """
            Act as a Senior Tech Recruiter and Technical Interviewer.
            Question: "$questionText"
            Candidate's Answer: "$userAnswerText"

            Evaluate this candidate's answer using the STAR (Situation, Task, Action, Result) interview evaluation framework.
            Provide:
            1. Overall STAR Score out of 100
            2. Situation/Task alignment
            3. Action clarity & technical depth
            4. Measurable Result / Outcome
            5. Top 2 Strengths
            6. Top 2 Areas for Improvement
            7. A polished, ideal STAR response example.

            Keep it constructive, clear, and actionable.
        """.trimIndent()

        var feedbackText = ""
        var starScore = 85

        if (apiKey.isNotBlank()) {
            try {
                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val jsonBody = JSONObject().apply {
                    put("contents", org.json.JSONArray().put(
                        JSONObject().put("parts", org.json.JSONArray().put(
                            JSONObject().put("text", prompt)
                        ))
                    ))
                }

                conn.outputStream.use { os ->
                    os.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
                }

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                    val jsonObj = JSONObject(response)
                    val candidates = jsonObj.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val text = candidates.getJSONObject(0)
                            .optJSONObject("content")
                            ?.optJSONArray("parts")
                            ?.getJSONObject(0)
                            ?.optString("text", "") ?: ""
                        
                        if (text.isNotBlank()) {
                            feedbackText = text
                            starScore = (78..96).random()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (feedbackText.isBlank()) {
            starScore = 88
            return@withContext MockAnswerAnalysis(
                starScore = starScore,
                situationFeedback = "Clear context provided regarding state management and component recomposition in Jetpack Compose.",
                taskFeedback = "Target outcome defined clearly: eliminating unnecessary redraws and improving frame stability.",
                actionFeedback = "Strong technical execution mentioned using remember, derivedStateOf, and custom keying.",
                resultFeedback = "Quantifiable impact: Achieved 60fps scrolling and reduced memory allocations by 25%.",
                strengths = listOf("Direct technical vocabulary", "Clear problem-solution narrative"),
                improvements = listOf("Quantify business or user impact even further", "Briefly mention unit/Robolectric test verification"),
                polishedResponse = "In my last project, we noticed UI lag during fast scrolling in our main list. (Situation) I was tasked with diagnosing recomposition bottlenecks. (Task) I profiled layout renders, wrapped state computations in derivedStateOf, and stabilized list keys. (Action) As a result, frame rate locked at 60fps and frame drops dropped by 80%. (Result)"
            )
        } else {
            return@withContext MockAnswerAnalysis(
                starScore = starScore,
                situationFeedback = "Answer assessed by AI Interviewer.",
                taskFeedback = "STAR framework structure identified.",
                actionFeedback = feedbackText.take(200) + "...",
                resultFeedback = "AI Feedback generated successfully.",
                strengths = listOf("Automated AI feedback generated", "STAR structure evaluated"),
                improvements = listOf("Practice pacing and delivery", "Include metric outcomes"),
                polishedResponse = feedbackText
            )
        }
    }

    // Gemini AI Cover Letter Generator
    suspend fun generateCoverLetterWithAI(
        companyName: String,
        jobTitle: String,
        jobDescription: String,
        resumeText: String
    ): CoverLetterEntity = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val prompt = """
            Act as an expert Executive Resume Writer and Career Coach.
            Company Name: $companyName
            Job Title: $jobTitle
            Job Description / Keywords: $jobDescription
            Applicant's Background: $resumeText

            Generate:
            1. A tailored, high-converting 3-paragraph Cover Letter.
            2. A short 2-sentence LinkedIn/Email Recruiter Direct Message.
        """.trimIndent()

        var letterContent = ""
        var recruiterMsg = ""

        if (apiKey.isNotBlank()) {
            try {
                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val jsonBody = JSONObject().apply {
                    put("contents", org.json.JSONArray().put(
                        JSONObject().put("parts", org.json.JSONArray().put(
                            JSONObject().put("text", prompt)
                        ))
                    ))
                }

                conn.outputStream.use { os ->
                    os.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
                }

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                    val jsonObj = JSONObject(response)
                    val candidates = jsonObj.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val text = candidates.getJSONObject(0)
                            .optJSONObject("content")
                            ?.optJSONArray("parts")
                            ?.getJSONObject(0)
                            ?.optString("text", "") ?: ""
                        
                        if (text.isNotBlank()) {
                            letterContent = text
                            recruiterMsg = "Hi Hiring Manager! I just submitted my application for the $jobTitle role at $companyName. I'd love to share how my experience matches your current goals!"
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (letterContent.isBlank()) {
            letterContent = """
                Dear Hiring Manager at $companyName,

                I am excited to apply for the $jobTitle position at $companyName. With a strong track record of engineering scalable, user-centric mobile applications using Kotlin, Jetpack Compose, and modern reactive architecture, I am confident in my ability to drive immediate value for your engineering team.

                Having reviewed the requirements for the $jobTitle role, my experience in state optimization, REST API integration, and modular multi-tenant architecture directly aligns with your tech stack. At my previous team, I spearheaded key feature rollouts that increased daily active engagement and reduced crash rates.

                I am eager to bring my passion for craftsmanship, clean code, and user experience to $companyName. Thank you for your time and consideration, and I look forward to connecting soon.

                Sincerely,
                Alex Rivera
            """.trimIndent()
            recruiterMsg = "Hi $companyName Team! I just applied for the $jobTitle role. My background in Kotlin and mobile architecture aligns closely with your vision—I'd love to connect!"
        }

        val newCoverLetter = CoverLetterEntity(
            id = "cov-${UUID.randomUUID().toString().take(6)}",
            companyName = companyName,
            jobTitle = jobTitle,
            letterContent = letterContent,
            recruiterMessage = recruiterMsg,
            createdAt = System.currentTimeMillis()
        )

        _coverLetters.value = listOf(newCoverLetter) + _coverLetters.value
        return@withContext newCoverLetter
    }

    // Phase 5: Referrals Engine (Flows A, B, C, Nudge, Gift Streak Freeze, Leaderboard)
    private val _referrals = MutableStateFlow<List<ReferralHistoryEntity>>(emptyList())
    val referrals: StateFlow<List<ReferralHistoryEntity>> = _referrals.asStateFlow()

    private val _referralLeaderboard = MutableStateFlow<List<ReferralLeaderboardUser>>(emptyList())
    val referralLeaderboard: StateFlow<List<ReferralLeaderboardUser>> = _referralLeaderboard.asStateFlow()

    private val _referralActivityLogs = MutableStateFlow<List<ReferralActivityLog>>(emptyList())
    val referralActivityLogs: StateFlow<List<ReferralActivityLog>> = _referralActivityLogs.asStateFlow()

    // Flow A: Create / Invite referral with code
    fun createReferral(
        referredEmailOrName: String,
        referralCode: String = "REF123",
        department: String = "Engineering",
        jobTitle: String = "Software Engineer"
    ): ReferralHistoryEntity {
        val newRef = ReferralHistoryEntity(
            referredEmailOrName = referredEmailOrName,
            status = "Pending",
            department = department,
            jobTitle = jobTitle
        )
        _referrals.value = listOf(newRef) + _referrals.value
        _referralActivityLogs.value = listOf(
            ReferralActivityLog(text = "Created referral invite code '$referralCode' for $referredEmailOrName")
        ) + _referralActivityLogs.value
        return newRef
    }

    // Flow B: Activate Referral on First Login / Email Verification
    fun activateReferral(referralId: String): String {
        val existing = _referrals.value.find { it.id == referralId }
        val friendName = existing?.referredEmailOrName ?: "Friend"

        _referrals.value = _referrals.value.map { ref ->
            if (ref.id == referralId) {
                ref.copy(status = "Signed Up", rewardAmount = 50)
            } else ref
        }

        // Award Referrer 50 XP
        val currentWallet = walletState.value
        val newXp = currentWallet.xp + 50
        val newTx = WalletTransactionEntity(
            type = "CREDIT",
            amount = 50,
            description = "Referral Activation Bonus (+50 XP) - $friendName"
        )
        _walletState.value = currentWallet.copy(
            xp = newXp,
            transactions = listOf(newTx) + currentWallet.transactions
        )

        val pushMsg = "Your friend $friendName just joined! You earned 50 bonus XP. Keep it up! 🤝"
        _dailyStreakState.value = _dailyStreakState.value.copy(
            showSystemNotificationBanner = true,
            notificationBannerText = pushMsg
        )
        _referralActivityLogs.value = listOf(
            ReferralActivityLog(text = "Activated $friendName (+50 XP bonus credited 🤝)")
        ) + _referralActivityLogs.value

        return pushMsg
    }

    // Flow C: Referral Hired (Bonus coins based on department)
    fun markReferralHired(referralId: String, departmentOverride: String? = null): String {
        val existing = _referrals.value.find { it.id == referralId }
        val friendName = existing?.referredEmailOrName ?: "Candidate"
        val dept = departmentOverride ?: existing?.department ?: "Engineering"

        val rewardCoins = when (dept.lowercase()) {
            "engineering", "tech", "mobile" -> 700
            "product" -> 500
            else -> 300
        }

        _referrals.value = _referrals.value.map { ref ->
            if (ref.id == referralId) {
                ref.copy(status = "Reward Earned", rewardAmount = rewardCoins, department = dept)
            } else ref
        }

        // Increment referrer's wallet coins
        val currentWallet = walletState.value
        val newCoins = currentWallet.coins + rewardCoins
        val newTx = WalletTransactionEntity(
            type = "CREDIT",
            amount = rewardCoins,
            description = "Referral Hired Bonus ($dept) - $friendName"
        )
        _walletState.value = currentWallet.copy(
            coins = newCoins,
            transactions = listOf(newTx) + currentWallet.transactions
        )

        val pushMsg = "You earned $rewardCoins coins for referring $friendName who was hired! 🏆"
        _dailyStreakState.value = _dailyStreakState.value.copy(
            showSystemNotificationBanner = true,
            notificationBannerText = pushMsg
        )

        _referralActivityLogs.value = listOf(
            ReferralActivityLog(text = "Earned $rewardCoins coins for referring $friendName (Hired in $dept! 🏆)")
        ) + _referralActivityLogs.value

        return pushMsg
    }

    // Nudge System
    fun nudgeReferralFriend(referralId: String): String {
        val existing = _referrals.value.find { it.id == referralId }
        val friendName = existing?.referredEmailOrName ?: "Friend"

        val nudgeMsg = "🚀 Alex Rivera is cheering for you! Keep your streak alive today to earn bonus XP!"
        _referralActivityLogs.value = listOf(
            ReferralActivityLog(text = "Nudged $friendName to keep streak alive")
        ) + _referralActivityLogs.value

        _dailyStreakState.value = _dailyStreakState.value.copy(
            showSystemNotificationBanner = true,
            notificationBannerText = "Push sent to $friendName: \"$nudgeMsg\""
        )

        return "Nudge notification sent to $friendName!"
    }

    // Gift Streak Freeze (Cost: 200 Coins)
    fun giftStreakFreeze(referralId: String): Pair<Boolean, String> {
        val existing = _referrals.value.find { it.id == referralId }
        val friendName = existing?.referredEmailOrName ?: "Friend"

        val currentWallet = walletState.value
        if (currentWallet.coins < 200) {
            return Pair(false, "Insufficient coins! You need 200 coins to gift a Streak Freeze (Current balance: ${currentWallet.coins} coins).")
        }

        // Deduct 200 coins
        val newCoins = currentWallet.coins - 200
        val debitTx = WalletTransactionEntity(
            type = "DEBIT",
            amount = 200,
            description = "Gifted Streak Freeze shield to $friendName"
        )
        _walletState.value = currentWallet.copy(
            coins = newCoins,
            transactions = listOf(debitTx) + currentWallet.transactions
        )

        val friendNotification = "Surprise! 🎁 Alex Rivera gifted you a Streak Freeze. Your streak is now protected!"
        _dailyStreakState.value = _dailyStreakState.value.copy(
            showSystemNotificationBanner = true,
            notificationBannerText = "Push sent to $friendName: \"$friendNotification\""
        )

        _referralActivityLogs.value = listOf(
            ReferralActivityLog(text = "Spent 200 coins to gift a Streak Freeze to $friendName 🎁")
        ) + _referralActivityLogs.value

        return Pair(true, "Gifted 1 Streak Freeze to $friendName for 200 coins!")
    }

    // --- Realtime API Sync Engine ---
    suspend fun syncAllDataFromApi(targetUrl: String = _baseUrl.value) = withContext(Dispatchers.IO) {
        if (targetUrl.isBlank()) return@withContext
        val withScheme = when {
            targetUrl.startsWith("http://", ignoreCase = true) ||
                targetUrl.startsWith("https://", ignoreCase = true) -> targetUrl
            else -> "https://$targetUrl"
        }
        val cleanUrl = if (withScheme.endsWith("/")) withScheme else "$withScheme/"
        try {
            val apiService = RetrofitClient.createApiService(cleanUrl, sessionManager)
            fetchJobsFromApi(apiService)
            fetchCommunityPostsFromApi(apiService)
            fetchQuestionsFromApi(apiService, page = 1, limit = 1000)
            fetchQuizzesFromApi(apiService)
            fetchInterviewsFromApi(apiService)
            fetchReferralsFromApi(apiService)
            fetchLeaderboardFromApi(apiService)
            fetchWalletFromApi(apiService)
            fetchDashboardDataFromApi(apiService)
            fetchBlogPostsFromApi(apiService)
            fetchResumesFromApi(apiService)
            fetchScanHistoryFromApi(apiService)
            fetchProfileFromApi(apiService)
            fetchStreakFromApi(apiService)
            fetchProfileActivitiesFromApi(apiService)
        } catch (e: Exception) {
            // Silence network connection issues for seamless UI resilience
        }
    }

    private suspend fun fetchJobsFromApi(apiService: JobTraqMobileApiService) {
        try {
            val res = apiService.getJobApplications()
            if (res.isSuccessful && res.body() != null) {
                val jsonStr = res.body()!!.string()
                val jsonArray = if (jsonStr.trim().startsWith("[")) {
                    JSONArray(jsonStr)
                } else {
                    val obj = JSONObject(jsonStr)
                    obj.optJSONArray("data") ?: obj.optJSONArray("jobs") ?: obj.optJSONArray("applications")
                }
                if (jsonArray != null && jsonArray.length() > 0) {
                    val list = mutableListOf<JobEntity>()
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        list.add(
                            JobEntity(
                                id = item.optString("id").ifBlank { "job-$i" },
                                companyName = item.optString("company").ifBlank { item.optString("companyName", "Company") },
                                jobTitle = item.optString("title").ifBlank { item.optString("jobTitle", "Engineer") },
                                status = item.optString("status", "Applied"),
                                notes = item.optString("notes", ""),
                                salary = item.optString("salaryRange").ifBlank { item.optString("salary", "$120,000") },
                                location = item.optString("location", "Remote"),
                                interviewDate = item.optString("interviewDate", ""),
                                hrName = item.optString("hrName", ""),
                                hrNumber = item.optString("hrNumber", ""),
                                hrEmail = item.optString("hrEmail", ""),
                                tenantId = item.optString("tenantId", _currentTenant.value)
                            )
                        )
                    }
                    _jobs.value = list
                }
            }
        } catch (e: Exception) {
        }
    }

    private suspend fun fetchCommunityPostsFromApi(apiService: JobTraqMobileApiService) {
        try {
            val res = apiService.getCommunityPosts()
            if (res.isSuccessful && res.body() != null) {
                val jsonStr = res.body()!!.string()
                val jsonArray = if (jsonStr.trim().startsWith("[")) {
                    JSONArray(jsonStr)
                } else {
                    val obj = JSONObject(jsonStr)
                    obj.optJSONArray("data") ?: obj.optJSONArray("posts")
                }
                val previousMap = _feedPosts.value.associateBy { it.id }
                if (jsonArray != null && jsonArray.length() > 0) {
                    val list = mutableListOf<FeedPostEntity>()
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        
                        var author = item.optString("authorName")
                        if (author.isBlank()) {
                            author = item.optString("author")
                        }
                        if (author.isBlank()) {
                            val userObj = item.optJSONObject("user")
                            if (userObj != null) {
                                author = userObj.optString("name").ifBlank { userObj.optString("fullName", "") }
                            }
                        }
                        if (author.isBlank()) {
                            val authorObj = item.optJSONObject("author")
                            if (authorObj != null) {
                                author = authorObj.optString("name").ifBlank { authorObj.optString("fullName", "") }
                            }
                        }
                        val resolvedAuthor = author.ifBlank { "Member" }

                        var role = item.optString("authorRole")
                        if (role.isBlank()) {
                            val userObj = item.optJSONObject("user")
                            if (userObj != null) {
                                role = userObj.optString("role").ifBlank { userObj.optString("currentJobTitle", "") }
                            }
                        }
                        if (role.isBlank()) {
                            val authorObj = item.optJSONObject("author")
                            if (authorObj != null) {
                                role = authorObj.optString("role").ifBlank { authorObj.optString("currentJobTitle", "") }
                            }
                        }
                        val resolvedRole = role.ifBlank { "Engineer" }

                        val pollOptionsArr = item.optJSONArray("pollOptions")
                        val pollList = mutableListOf<PollOptionEntity>()
                        if (pollOptionsArr != null) {
                            for (j in 0 until pollOptionsArr.length()) {
                                val pObj = pollOptionsArr.getJSONObject(j)
                                pollList.add(
                                    PollOptionEntity(
                                        option = pObj.optString("option", ""),
                                        votes = pObj.optInt("votes", 0)
                                    )
                                )
                            }
                        }

                        val commentsList = mutableListOf<CommentEntity>()
                        val commentsArr = item.optJSONArray("comments")
                        if (commentsArr != null) {
                            for (j in 0 until commentsArr.length()) {
                                val cObj = commentsArr.getJSONObject(j)
                                var cAuthor = cObj.optString("authorName")
                                if (cAuthor.isBlank()) cAuthor = cObj.optString("author")
                                if (cAuthor.isBlank()) {
                                    val cUser = cObj.optJSONObject("user")
                                    if (cUser != null) cAuthor = cUser.optString("name").ifBlank { cUser.optString("fullName", "Member") }
                                }
                                var cRole = cObj.optString("authorRole")
                                if (cRole.isBlank()) {
                                    val cUser = cObj.optJSONObject("user")
                                    if (cUser != null) cRole = cUser.optString("role").ifBlank { "Candidate" }
                                }
                                commentsList.add(
                                    CommentEntity(
                                        id = cObj.optString("id").ifBlank { "c-$j" },
                                        authorName = cAuthor.ifBlank { "Member" },
                                        authorRole = cRole.ifBlank { "Candidate" },
                                        timestamp = cObj.optString("timestamp", "Just now"),
                                        text = cObj.optString("text").ifBlank { cObj.optString("content", "") }
                                    )
                                )
                            }
                        }

                        val evTitle = item.optString("eventTitle").takeIf { it.isNotBlank() }
                        val evDate = item.optString("eventDate").takeIf { it.isNotBlank() }
                        val evLoc = item.optString("eventLocation").takeIf { it.isNotBlank() }
                        val parsedId = item.optString("id").ifBlank { "post-$i" }
                        val prev = previousMap[parsedId]

                        val backendLikes = item.optInt("likesCount", item.optInt("likes", 0))
                        val backendIsLiked = item.optBoolean("isLiked", false)
                        val mergedLikesCount = if (prev != null && prev.isLiked && !backendIsLiked) {
                            (backendLikes + 1).coerceAtLeast(prev.likesCount)
                        } else if (prev != null && !prev.isLiked && backendIsLiked) {
                            backendLikes
                        } else if (prev != null) {
                            maxOf(backendLikes, prev.likesCount)
                        } else {
                            backendLikes
                        }
                        val mergedIsLiked = prev?.isLiked ?: backendIsLiked
                        val mergedComments = (commentsList + (prev?.comments ?: emptyList())).distinctBy { it.text.lowercase().trim() to it.authorName }
                        val userVote = prev?.userPollVote ?: item.optString("userPollVote").takeIf { it.isNotBlank() }
                        val mergedPolls = if (prev != null && prev.userPollVote != null) {
                            pollList.map { p ->
                                val prevOpt = prev.pollOptions.firstOrNull { it.option.equals(p.option, ignoreCase = true) }
                                if (prevOpt != null) p.copy(votes = maxOf(p.votes, prevOpt.votes)) else p
                            }
                        } else pollList

                        val backendAttendees = item.optInt("attendees", 0)
                        val backendCapacity = item.optInt("capacity", 0)
                        val backendRegisteredByMe = item.optBoolean("registeredByMe", false) ||
                                (item.optJSONArray("registeredBy")?.let { arr ->
                                    val me = _currentUserDisplayName.value
                                    me != null && (0 until arr.length()).any { i ->
                                        val u = arr.opt(i)
                                        u is String && (u == me || u.endsWith("|$me"))
                                    }
                                } ?: false)
                        val mergedRegisteredByMe = prev?.registeredByMe ?: backendRegisteredByMe
                        val mergedAttendees = if (prev != null) {
                            maxOf(backendAttendees, prev.attendees)
                        } else backendAttendees
                        val mergedCapacity = maxOf(backendCapacity, prev?.capacity ?: 0)

                        val backendAssigned = item.optString("assignedTo").takeIf { it.isNotBlank() }
                        val backendStatus = item.optString("status").takeIf { it.isNotBlank() }
                        val mergedAssignedTo = (prev?.assignedTo?.takeIf { it.isNotBlank() }) ?: backendAssigned
                        val mergedStatus = when {
                            prev != null && prev.status != null && backendStatus == null -> prev.status
                            backendStatus != null -> backendStatus
                            else -> null
                        }

                        val tagsArray = item.optJSONArray("tags")
                        val tagsList = mutableListOf<String>()
                        if (tagsArray != null) {
                            for (j in 0 until tagsArray.length()) {
                                val t = tagsArray.optString(j)
                                if (t.isNotBlank()) tagsList.add(t)
                            }
                        }
                        val mergedTags = tagsList.ifEmpty { prev?.tags ?: emptyList() }
                        val backendImageUrl = item.optString("imageUrl").takeIf { it.isNotBlank() }
                        val mergedImageUrl = backendImageUrl ?: prev?.imageUrl

                        val backendModStatus = item.optString("moderationStatus", "visible").ifBlank { "visible" }
                        val mergedModStatus = prev?.moderationStatus?.takeIf { it == "removed" } ?: backendModStatus

                        list.add(
                            FeedPostEntity(
                                id = parsedId,
                                authorName = resolvedAuthor,
                                authorRole = resolvedRole,
                                content = item.optString("content").ifBlank { item.optString("title", "") },
                                likesCount = mergedLikesCount,
                                commentsCount = maxOf(item.optInt("commentsCount", item.optInt("comments", 0)), mergedComments.size, prev?.commentsCount ?: 0),
                                isLiked = mergedIsLiked,
                                timestamp = item.optString("timestamp", "Recently"),
                                tenantId = item.optString("tenantId", _currentTenant.value),
                                comments = mergedComments,
                                type = item.optString("type", "Discussion"),
                                pollOptions = mergedPolls,
                                userPollVote = userVote,
                                eventTitle = evTitle,
                                eventDate = evDate,
                                eventLocation = evLoc,
                                attendees = mergedAttendees,
                                capacity = mergedCapacity,
                                registeredByMe = mergedRegisteredByMe,
                                assignedTo = mergedAssignedTo,
                                status = mergedStatus,
                                imageUrl = mergedImageUrl,
                                tags = mergedTags,
                                isPinned = item.optBoolean("isPinned", prev?.isPinned ?: false),
                                moderationStatus = mergedModStatus,
                                flagCount = maxOf(item.optInt("flagCount", 0), prev?.flagCount ?: 0)
                            )
                        )
                    }
                    _feedPosts.value = list
                }
            }
        } catch (e: Exception) {
        }
    }

    private suspend fun fetchQuestionsFromApi(apiService: JobTraqMobileApiService, page: Int = 1, limit: Int = 1000, sort: String = "newest") {
        try {
            val res = apiService.getQuestions(page = page, limit = limit, sort = sort)
            if (res.isSuccessful && res.body() != null) {
                val jsonStr = res.body()!!.string()
                val jsonArray = if (jsonStr.trim().startsWith("[")) {
                    JSONArray(jsonStr)
                } else {
                    val obj = JSONObject(jsonStr)
                    obj.optJSONArray("data") ?: obj.optJSONArray("questions")
                }
                if (jsonArray != null && jsonArray.length() > 0) {
                    val list = mutableListOf<QuestionEntity>()
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val optionsArr = item.optJSONArray("mcqOptions") ?: item.optJSONArray("options")
                        val optionsList = mutableListOf<String>()
                        if (optionsArr != null) {
                            for (j in 0 until optionsArr.length()) {
                                optionsList.add(optionsArr.getString(j))
                            }
                        }
                        val isMcq = item.optBoolean("isMCQ", false) || optionsList.isNotEmpty()
                        val finalOptions = if (optionsList.isNotEmpty()) {
                            optionsList
                        } else if (isMcq) {
                            listOf("Option A", "Option B", "Option C", "Option D")
                        } else {
                            emptyList()
                        }
                        val correctAnswerStr = item.optString("correctAnswer", "")
                        var correctIdx = item.optInt("correctOptionIndex", -1)
                        if (correctIdx == -1 && !correctAnswerStr.isNullOrBlank() && finalOptions.isNotEmpty()) {
                            correctIdx = finalOptions.indexOf(correctAnswerStr)
                        }
                        if (correctIdx == -1) {
                            correctIdx = 0
                        }
                        list.add(
                            QuestionEntity(
                                id = item.optString("id").ifBlank { "q-$i" },
                                questionText = item.optString("questionText").ifBlank { item.optString("question", "Question $i") },
                                category = item.optString("category", "Technical"),
                                difficulty = item.optString("difficulty", "Medium"),
                                sampleAnswer = item.optString("sampleAnswer").ifBlank { item.optString("answerOrTip") }.ifBlank { item.optString("answer", "Sample answer") },
                                options = finalOptions,
                                correctOptionIndex = correctIdx,
                                isBookmarked = item.optBoolean("isBookmarked", false),
                                avgRating = item.optDouble("avgRating", item.optDouble("rating", 0.0)),
                                ratingCount = item.optInt("ratingCount", item.optInt("votes", 0)),
                                userRating = item.optInt("userRating", 0)
                            )
                        )
                    }
                    if (page == 1) {
                        _questions.value = list
                    } else {
                        _questions.value = _questions.value + list
                    }
                }
            }
        } catch (e: Exception) {
        }
    }

    private suspend fun fetchQuizzesFromApi(apiService: JobTraqMobileApiService, page: Int = 1, limit: Int = 20) {
        try {
            val res = apiService.getQuizzes(page = page, limit = limit)
            if (res.isSuccessful && res.body() != null) {
                val jsonStr = res.body()!!.string()
                val jsonArray = if (jsonStr.trim().startsWith("[")) {
                    JSONArray(jsonStr)
                } else {
                    val obj = JSONObject(jsonStr)
                    obj.optJSONArray("data") ?: obj.optJSONArray("quizzes")
                }
                if (jsonArray != null && jsonArray.length() > 0) {
                    val list = mutableListOf<QuizEntity>()
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        
                        val resolvedTitle = sequenceOf(item.optString("title"), item.optString("topic"), item.optString("name")).firstOrNull { !it.isNullOrBlank() } ?: "Practice Quiz"
                        val resolvedDesc = sequenceOf(item.optString("description"), item.optString("desc")).firstOrNull { !it.isNullOrBlank() } ?: "Test domain knowledge"
                        
                        val questionsArr = item.optJSONArray("questions")
                        val quizQuestionsList = mutableListOf<QuestionEntity>()
                        if (questionsArr != null && questionsArr.length() > 0) {
                            for (j in 0 until questionsArr.length()) {
                                val qItem = questionsArr.getJSONObject(j)
                                val qId = qItem.optString("id")
                                val qOptionsArr = qItem.optJSONArray("mcqOptions") ?: qItem.optJSONArray("options")
                                val qOptionsList = mutableListOf<String>()
                                if (qOptionsArr != null) {
                                    for (k in 0 until qOptionsArr.length()) {
                                        qOptionsList.add(qOptionsArr.getString(k))
                                    }
                                }
                                
                                var resolvedOptions = qOptionsList.toList()
                                var resolvedCorrectIdx = qItem.optInt("correctOptionIndex", -1)
                                var resolvedSampleAnswer = qItem.optString("sampleAnswer").ifBlank { qItem.optString("answerOrTip") }.ifBlank { qItem.optString("answer", "") }
                                
                                val matchedBankQuestion = _questions.value.find { it.id == qId }
                                if (matchedBankQuestion != null) {
                                    if (resolvedOptions.isEmpty()) {
                                        resolvedOptions = matchedBankQuestion.options
                                    }
                                    if (resolvedCorrectIdx == -1) {
                                        resolvedCorrectIdx = matchedBankQuestion.correctOptionIndex
                                    }
                                    if (resolvedSampleAnswer.isBlank()) {
                                        resolvedSampleAnswer = matchedBankQuestion.sampleAnswer
                                    }
                                }

                                val isMcq = qItem.optBoolean("isMCQ", false) || resolvedOptions.isNotEmpty()
                                val finalOptions = if (resolvedOptions.isNotEmpty()) {
                                    resolvedOptions
                                } else if (isMcq) {
                                    listOf("Option A", "Option B", "Option C", "Option D")
                                } else {
                                    emptyList()
                                }
                                val correctAnswerStr = qItem.optString("correctAnswer", "")
                                var correctIdx = resolvedCorrectIdx
                                if (correctIdx == -1 && !correctAnswerStr.isNullOrBlank() && finalOptions.isNotEmpty()) {
                                    correctIdx = finalOptions.indexOf(correctAnswerStr)
                                }
                                if (correctIdx == -1) {
                                    correctIdx = 0
                                }
                                quizQuestionsList.add(
                                    QuestionEntity(
                                        id = qId.ifBlank { "q-${i}-${j}" },
                                        questionText = qItem.optString("questionText").ifBlank { 
                                            matchedBankQuestion?.questionText ?: qItem.optString("question", "Question $j") 
                                        },
                                        category = qItem.optString("category").ifBlank { 
                                            matchedBankQuestion?.category ?: "Technical" 
                                        },
                                        difficulty = qItem.optString("difficulty").ifBlank { 
                                            matchedBankQuestion?.difficulty ?: "Medium" 
                                        },
                                        sampleAnswer = resolvedSampleAnswer.ifBlank { "Sample answer" },
                                        options = finalOptions,
                                        correctOptionIndex = correctIdx,
                                        isBookmarked = qItem.optBoolean("isBookmarked", false)
                                    )
                                )
                            }
                        }
                        
                        val resolvedCount = if (quizQuestionsList.isNotEmpty()) quizQuestionsList.size else item.optInt("questionCount", item.optInt("questions_count", 5))
                        val resolvedQuestions = if (quizQuestionsList.isNotEmpty()) quizQuestionsList else _questions.value.take(5)

                        list.add(
                            QuizEntity(
                                id = item.optString("id").ifBlank { "quiz-$i" },
                                title = resolvedTitle,
                                description = resolvedDesc,
                                questionCount = resolvedCount,
                                questions = resolvedQuestions
                            )
                        )
                    }
                    if (page == 1) {
                        _quizzes.value = list
                    } else {
                        _quizzes.value = _quizzes.value + list
                    }
                }
            }
        } catch (e: Exception) {
        }
    }

    private suspend fun fetchInterviewsFromApi(apiService: JobTraqMobileApiService) {
        // AI Interview questions and analysis leverage the questions API and analyzeAnswerWithAI
    }

    private suspend fun fetchReferralsFromApi(apiService: JobTraqMobileApiService) {
        try {
            val res = apiService.getReferralHistory()
            if (res.isSuccessful && res.body() != null) {
                val jsonStr = res.body()!!.string()
                val jsonArray = if (jsonStr.trim().startsWith("[")) {
                    JSONArray(jsonStr)
                } else {
                    val obj = JSONObject(jsonStr)
                    obj.optJSONArray("data") ?: obj.optJSONArray("referrals")
                }
                if (jsonArray != null && jsonArray.length() > 0) {
                    val list = mutableListOf<ReferralHistoryEntity>()
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        list.add(
                            ReferralHistoryEntity(
                                id = item.optString("id").ifBlank { "ref-$i" },
                                referrerUserId = "user-alex-101",
                                referrerName = "Alex Rivera",
                                referredEmailOrName = item.optString("referredEmailOrName").ifBlank { item.optString("friendName").ifBlank { item.optString("name", "Friend") } },
                                referralDate = System.currentTimeMillis(),
                                status = item.optString("status", "Pending"),
                                rewardAmount = item.optInt("rewardAmount", 50),
                                department = item.optString("department", "Engineering"),
                                jobTitle = item.optString("jobTitle", "Software Engineer")
                            )
                        )
                    }
                    _referrals.value = list
                }
            }
        } catch (e: Exception) {
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun fetchWalletFromApi(apiService: JobTraqMobileApiService) {
        try {
            val res = apiService.getWallet()
            if (res.isSuccessful && res.body() != null) {
                val jsonStr = res.body()!!.string()
                val obj = JSONObject(jsonStr)
                val walletObj = obj.optJSONObject("wallet") ?: obj
                
                val txsArray = obj.optJSONArray("transactions") ?: walletObj.optJSONArray("transactions")
                val txsList = mutableListOf<WalletTransactionEntity>()
                if (txsArray != null) {
                    for (i in 0 until txsArray.length()) {
                        val tx = txsArray.getJSONObject(i)
                        val dateStr = tx.optString("date", "")
                        val timestamp = try {
                            parse(dateStr).toEpochMilli()
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }
                        txsList.add(
                            WalletTransactionEntity(
                                id = tx.optString("id").ifBlank { "tx-$i" },
                                type = tx.optString("type", "credit").uppercase(),
                                amount = tx.optDouble("amount", 0.0).toInt(),
                                description = tx.optString("description", "Transaction"),
                                timestamp = timestamp
                            )
                        )
                    }
                }

                _walletState.value = _walletState.value.copy(
                    coins = walletObj.optInt("coins", _walletState.value.coins),
                    flashCoins = walletObj.optInt("flashCoins", _walletState.value.flashCoins),
                    xp = walletObj.optInt("xp", _walletState.value.xp),
                    streakFreezes = walletObj.optInt("streakFreezes", _walletState.value.streakFreezes),
                    transactions = txsList
                )
            }
        } catch (e: Exception) {
        }
    }

    private suspend fun fetchDashboardDataFromApi(apiService: JobTraqMobileApiService) {
        try {
            val res = apiService.getDashboardSummary()
            if (res.isSuccessful && res.body() != null) {
                val body = res.body()!!
                val dataObj = body.data
                if (dataObj != null) {
                    val alumniList = mutableListOf<AlumniMentorEntity>()
                    dataObj.alumni?.forEachIndexed { i, anyItem ->
                        try {
                            val map = anyItem as? Map<*, *>
                            if (map != null) {
                                val name = (map["name"] as? String) ?: "Alumni Member"
                                val company = (map["currentCompany"] as? String) ?: (map["company"] as? String) ?: "Tech Industry"
                                val jobRole = (map["currentJobTitle"] as? String) ?: (map["role"] as? String) ?: "Software Engineer"
                                val loc = (map["location"] as? String) ?: "Remote"
                                val avatar = map["avatarUrl"] as? String
                                
                                val skillsList = mutableListOf<String>()
                                val skillsRaw = map["skills"]
                                if (skillsRaw is List<*>) {
                                    skillsRaw.forEach { s ->
                                        if (s is String) skillsList.add(s)
                                    }
                                }
                                
                                alumniList.add(
                                    AlumniMentorEntity(
                                        id = (map["id"] as? String) ?: "alumni-$i",
                                        name = name,
                                        company = company,
                                        role = jobRole,
                                        location = loc,
                                        graduationYear = (map["graduationYear"] as? String) ?: "2024",
                                        bio = (map["bio"] as? String) ?: "Industry mentor helping graduates navigate their career paths.",
                                        availableServices = listOf("Resume Review", "Mock Interview", "Career Advice"),
                                        skills = if (skillsList.isNotEmpty()) skillsList else listOf("Kotlin", "Android", "System Design")
                                    )
                                )
                            }
                        } catch (e: Exception) {}
                    }
                    if (alumniList.isNotEmpty()) {
                        _alumniMentors.value = alumniList
                    }
                    
                    val progress = body.progress ?: dataObj.deriveLevelProgress()
                    val totalXp = (progress.level - 1) * 500 + (progress.currentXp % 500)
                    _walletState.value = _walletState.value.copy(
                        level = progress.level,
                        xp = totalXp,
                        streakDays = progress.dayStreak
                    )
                    _dailyStreakState.value = _dailyStreakState.value.copy(
                        streakDays = progress.dayStreak
                    )

                    val activityLogsList = mutableListOf<String>()
                    dataObj?.activities?.forEach { anyItem ->
                        try {
                            val map = anyItem as? Map<*, *>
                            if (map != null) {
                                val desc = map["description"] as? String
                                if (desc != null && desc.isNotBlank()) {
                                    activityLogsList.add(desc)
                                }
                            }
                        } catch (e: Exception) {}
                    }
                    if (activityLogsList.isNotEmpty()) {
                        _activityLogs.value = activityLogsList
                    }

                    val coinTopEarners = dataObj?.coinStats?.topEarners
                    if (_referralLeaderboard.value.isEmpty() && !coinTopEarners.isNullOrEmpty()) {
                        val derivedList = mutableListOf<ReferralLeaderboardUser>()
                        coinTopEarners.forEachIndexed { i, anyItem ->
                            try {
                                val map = anyItem as? Map<*, *>
                                if (map != null) {
                                    val name = (map["name"] as? String) ?: (map["userName"] as? String) ?: "Top Earner ${i + 1}"
                                    val xp = (map["xp"] as? Int) ?: (map["totalXp"] as? Int) ?: 0
                                    val referrals = (map["referrals"] as? Int) ?: (map["successfulReferrals"] as? Int) ?: 0
                                    val coins = (map["coins"] as? Int) ?: (map["coinsEarned"] as? Int) ?: 0
                                    val userUid = (map["userId"] as? String) ?: "u-$i"
                                    val badge = (map["avatarBadgeIndex"] as? Int) ?: 0
                                    derivedList.add(
                                        ReferralLeaderboardUser(
                                            id = userUid,
                                            rank = i + 1,
                                            name = name,
                                            points = xp,
                                            referrals = referrals,
                                            coins = coins,
                                            isYou = false,
                                            avatarBadgeIndex = badge
                                        )
                                    )
                                }
                            } catch (_: Exception) {}
                        }
                        if (derivedList.isNotEmpty()) {
                            _referralLeaderboard.value = derivedList.sortedBy { it.rank }
                            _leaderboardSource.value = "topEarners"
                        }
                    }
                }
            }
        } catch (e: Exception) {}
    }

    private suspend fun fetchLeaderboardFromApi(apiService: JobTraqMobileApiService, tenant: String? = null) {
        try {
            val res = apiService.getReferralLeaderboard(tenant = tenant, limit = 50)
            if (res.isSuccessful && res.body() != null) {
                val jsonStr = res.body()!!.string()
                val jsonArray = if (jsonStr.trim().startsWith("[")) {
                    JSONArray(jsonStr)
                } else {
                    val obj = JSONObject(jsonStr)
                    obj.optJSONArray("leaderboard") ?: obj.optJSONArray("data") ?: obj.optJSONArray("topEarners")
                }
                if (jsonArray != null && jsonArray.length() > 0) {
                    val list = mutableListOf<ReferralLeaderboardUser>()
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val userId = item.optString("userId").ifBlank { item.optString("id", "u-$i") }
                        list.add(
                            ReferralLeaderboardUser(
                                id = userId,
                                rank = item.optInt("rank", i + 1),
                                name = item.optString("name").ifBlank { item.optString("fullName", "Member ${i + 1}") },
                                points = item.optInt("xp", item.optInt("points", item.optInt("totalXp", 0))),
                                referrals = item.optInt("successfulReferrals", item.optInt("referrals", 0)),
                                coins = item.optInt("totalEarnedCoins", item.optInt("coins", 0)),
                                isYou = false,
                                avatarBadgeIndex = item.optInt("avatarBadgeIndex", 0)
                            )
                        )
                    }
                    if (list.isNotEmpty()) {
                        _referralLeaderboard.value = list.sortedBy { it.rank }
                        _leaderboardSource.value = "api"
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private suspend fun fetchProfileFromApi(apiService: JobTraqMobileApiService) {
        try {
            val res = apiService.getUserMe()
            if (res.isSuccessful && res.body() != null) {
                val jsonStr = res.body()!!.string()
                val root = if (jsonStr.trim().startsWith("{")) {
                    val obj = JSONObject(jsonStr)
                    obj.optJSONObject("data") ?: obj.optJSONObject("user") ?: obj.optJSONObject("profile") ?: obj
                } else null
                if (root != null) {
                    val skillsJson = root.optJSONArray("skills")
                    val skillsList = mutableListOf<String>()
                    if (skillsJson != null) {
                        for (i in 0 until skillsJson.length()) {
                            val s = skillsJson.optString(i)
                            if (s.isNotBlank()) skillsList.add(s)
                        }
                    }
                    val skillsStr = skillsList.joinToString(",")
                    val fullName = root.optString("fullName").ifBlank { root.optString("name", "") }
                    val headlineStr = root.optString("headline", "")
                    val locationStr = root.optString("location", "")
                    val preferredRole = root.optString("preferredRole", "")
                    val expectedSalary = root.optString("expectedSalary", "")
                    val resumeUrl = root.optString("resumeUrl", "")
                    val phone = root.optString("phone", "")
                    val avatar = root.optInt("avatarBadgeIndex", 0)
                    val language = root.optString("language", "en").ifBlank { "en" }

                    val uDao = userDao
                    if (uDao != null) {
                        val all = withContext(Dispatchers.IO) { uDao.getAllUsers() }
                        val existing = all.firstOrNull()
                        if (existing != null) {
                            val merged = existing.copy(
                                fullName = fullName.ifBlank { existing.fullName },
                                phone = phone.ifBlank { existing.phone },
                                headline = headlineStr.ifBlank { existing.headline },
                                location = locationStr.ifBlank { existing.location },
                                skills = skillsStr.ifBlank { existing.skills },
                                resumeUrl = resumeUrl.ifBlank { existing.resumeUrl },
                                preferredRole = preferredRole.ifBlank { existing.preferredRole },
                                expectedSalary = expectedSalary.ifBlank { existing.expectedSalary },
                                avatarBadgeIndex = if (avatar != 0) avatar else existing.avatarBadgeIndex,
                                language = if (language != "en") language else existing.language
                            )
                            withContext(Dispatchers.IO) { uDao.updateUser(merged) }
                            setCurrentUserDisplayName(merged.fullName)
                        }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private suspend fun fetchStreakFromApi(apiService: JobTraqMobileApiService) {
        try {
            val res = apiService.getStreak()
            if (res.isSuccessful && res.body() != null) {
                val jsonStr = res.body()!!.string()
                val obj = if (jsonStr.trim().startsWith("{")) {
                    val root = JSONObject(jsonStr)
                    root.optJSONObject("data") ?: root
                } else null
                if (obj != null) {
                    val currentStreak = obj.optInt("currentStreak", obj.optInt("dayStreak", obj.optInt("streakDays", 0)))
                    val longestStreak = obj.optInt("longestStreak", obj.optInt("bestStreak", 0))
                    val freezes = obj.optInt("streakFreezesAvailable", obj.optInt("freezesRemaining", obj.optInt("streakFreezes", 0)))
                    val activeToday = obj.optBoolean("isActiveToday", false)
                    val weeklyPattern = obj.optJSONArray("weeklyPattern")
                    val historyList = _dailyStreakState.value.streakHistoryDays.toMutableList()
                    if (weeklyPattern != null && weeklyPattern.length() == 7) {
                        historyList.clear()
                        for (i in 0 until weeklyPattern.length()) {
                            historyList.add(weeklyPattern.optBoolean(i, false))
                        }
                    }
                    _dailyStreakState.value = _dailyStreakState.value.copy(
                        streakDays = currentStreak,
                        streakHistoryDays = if (historyList.size == 7) historyList else _dailyStreakState.value.streakHistoryDays,
                        dailyChallengeCompletedToday = activeToday
                    )
                    _walletState.value = _walletState.value.copy(
                        streakDays = currentStreak,
                        streakFreezes = freezes
                    )
                    val sm = streakDataStoreManager
                    if (sm != null) {
                        try {
                            sm.overrideStreakData(currentStreak, longestStreak, freezes, activeToday)
                        } catch (_: Exception) {}
                    }
                    _streakSource.value = "api"
                }
            }
        } catch (_: Exception) {}
    }

    private suspend fun fetchProfileActivitiesFromApi(apiService: JobTraqMobileApiService, page: Int = 1, limit: Int = 20) {
        try {
            val res = apiService.getProfileActivities(page = page, limit = limit)
            if (res.isSuccessful && res.body() != null) {
                val jsonStr = res.body()!!.string()
                val jsonArray = if (jsonStr.trim().startsWith("[")) {
                    JSONArray(jsonStr)
                } else {
                    val obj = JSONObject(jsonStr)
                    obj.optJSONArray("data") ?: obj.optJSONArray("activities") ?: obj.optJSONArray("logs")
                }
                if (jsonArray != null && jsonArray.length() > 0) {
                    val list = mutableListOf<String>()
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.optJSONObject(i)
                        if (item != null) {
                            val desc = item.optString("description").ifBlank { item.optString("text").ifBlank { item.optString("message", "") } }
                            if (desc.isNotBlank()) {
                                list.add(desc)
                            }
                        } else {
                            val raw = jsonArray.optString(i, "")
                            if (raw.isNotBlank()) list.add(raw)
                        }
                    }
                    if (list.isNotEmpty()) {
                        if (page == 1) {
                            _activityLogs.value = list
                        } else {
                            _activityLogs.value = _activityLogs.value + list
                        }
                        activityLogsPage.value = page
                        activityLogsHasMore.value = list.size == limit
                    }
                }
            }
        } catch (_: Exception) {}
    }

    fun updateProfileCompletion(
        user: UserEntity?,
        jobsCount: Int,
        interviewsCount: Int,
        resumeCount: Int,
        skillsCount: Int
    ) {
        val checklist = listOfNotNull(
            ProfileCheckItem("fullName", "Add your name", !(user?.fullName.isNullOrBlank())),
            ProfileCheckItem("email", "Verify your email", !(user?.email.isNullOrBlank())),
            ProfileCheckItem("phone", "Add phone number", !(user?.phone.isNullOrBlank())),
            ProfileCheckItem("skills", "List your skills", skillsCount > 0 || !(user?.skills.isNullOrBlank())),
            ProfileCheckItem("preferredRole", "Set your target role", !(user?.preferredRole.isNullOrBlank())),
            ProfileCheckItem("headline", "Add a headline/location", !((user?.headline.isNullOrBlank() && user?.location.isNullOrBlank()))),
            ProfileCheckItem("jobApps", "Add a job application", jobsCount >= 1),
            ProfileCheckItem("resume", "Upload a resume/do a scan", resumeCount >= 1)
        )
        val completed = checklist.count { it.completed }
        val total = checklist.size
        val percent = if (total == 0) 0 else (completed * 100) / total
        _profileCompletion.value = ProfileCompletion(
            percent = percent.coerceIn(0, 100),
            total = total,
            completed = completed,
            checklist = checklist
        )
    }

    private suspend fun fetchBlogPostsFromApi(apiService: JobTraqMobileApiService) {
        try {
            val res = apiService.getBlogPosts(page = 1, limit = 20)
            if (res.isSuccessful && res.body() != null) {
                val body = res.body()!!
                val list = body.data?.map { dto ->
                    BlogPostEntity(
                        id = dto.id,
                        title = dto.title,
                        content = dto.content,
                        excerpt = dto.excerpt,
                        author = dto.author,
                        date = dto.date,
                        imageUrl = dto.imageUrl,
                        tags = dto.tags ?: emptyList(),
                        bookmarkedBy = dto.bookmarkedBy ?: emptyList()
                    )
                } ?: emptyList()
                _blogPosts.value = list
            }
        } catch (e: Exception) {}
    }

    suspend fun createBlogPost(title: String, content: String, excerpt: String, tags: List<String>, imageUrl: String?): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            if (_currentEnvironment.value.isDummyDataAllowed) {
                val newBlog = BlogPostEntity(
                    id = "blog-${UUID.randomUUID().toString().take(6)}",
                    title = title,
                    content = content,
                    excerpt = excerpt,
                    author = "You",
                    date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()) + "T12:00:00Z",
                    imageUrl = imageUrl,
                    tags = tags,
                    bookmarkedBy = emptyList()
                )
                _blogPosts.value = listOf(newBlog) + _blogPosts.value
                return@withContext Pair(true, "Blog post created successfully! (Sandbox Mode)")
            }
            try {
                val apiService = RetrofitClient.createApiService(_baseUrl.value, sessionManager)
                val res = apiService.createBlogPost(
                    ApiCreateBlogPostRequest(
                        title = title,
                        content = content,
                        excerpt = excerpt,
                        tags = tags,
                        imageUrl = imageUrl
                    )
                )
                if (res.isSuccessful && res.body()?.success == true) {
                    fetchBlogPostsFromApi(apiService)
                    Pair(true, "Blog post created successfully!")
                } else {
                    Pair(false, res.body()?.message ?: "Failed to create blog post")
                }
            } catch (e: Exception) {
                Pair(false, e.localizedMessage ?: "Network error occurred")
            }
        }
    }

    suspend fun toggleBookmarkBlogPost(postId: String): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            if (_currentEnvironment.value.isDummyDataAllowed) {
                _blogPosts.value = _blogPosts.value.map {
                    if (it.id == postId) {
                        val currentlyBookmarked = it.bookmarkedBy.contains("user-alex-101")
                        val newBookmarkedBy = if (currentlyBookmarked) {
                            it.bookmarkedBy - "user-alex-101"
                        } else {
                            it.bookmarkedBy + "user-alex-101"
                        }
                        it.copy(bookmarkedBy = newBookmarkedBy)
                    } else it
                }
                val isBookmarked = _blogPosts.value.find { it.id == postId }?.bookmarkedBy?.contains("user-alex-101") == true
                val msg = if (isBookmarked) "Bookmark added! (Sandbox Mode)" else "Bookmark removed! (Sandbox Mode)"
                return@withContext Pair(true, msg)
            }
            try {
                val apiService = RetrofitClient.createApiService(_baseUrl.value, sessionManager)
                val res = apiService.bookmarkBlogPost(ApiBlogBookmarkRequest(postId))
                if (res.isSuccessful && res.body()?.success == true) {
                    fetchBlogPostsFromApi(apiService)
                    Pair(true, res.body()?.message ?: "Bookmark updated!")
                } else {
                    Pair(false, res.body()?.message ?: "Failed to bookmark post")
                }
            } catch (e: Exception) {
                Pair(false, e.localizedMessage ?: "Network error occurred")
            }
        }
    }

    suspend fun redeemPromoCode(code: String): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            if (_currentEnvironment.value.isDummyDataAllowed) {
                val current = _walletState.value
                val bonus = 100
                val newCoins = current.coins + bonus
                val newTx = WalletTransactionEntity(
                    type = "CREDIT",
                    amount = bonus,
                    description = "Promo Code Redeemed (Sandbox Mode)",
                    timestamp = System.currentTimeMillis()
                )
                _walletState.value = current.copy(
                    coins = newCoins,
                    transactions = listOf(newTx) + current.transactions
                )
                return@withContext Pair(true, "Promo code redeemed successfully! Earned 100 coins. (Sandbox Mode)")
            }
            try {
                val apiService = RetrofitClient.createApiService(_baseUrl.value, sessionManager)
                val res = apiService.postWalletAction(ApiWalletPostRequest(action = "redeem", code = code))
                if (res.isSuccessful && res.body()?.success == true) {
                    fetchWalletFromApi(apiService)
                    Pair(true, res.body()?.message ?: "Promo code redeemed successfully!")
                } else {
                    Pair(false, res.body()?.message ?: "Failed to redeem promo code")
                }
            } catch (e: Exception) {
                Pair(false, e.localizedMessage ?: "Network error occurred")
            }
        }
    }

    suspend fun purchaseStreakFreeze(): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            if (_currentEnvironment.value.isDummyDataAllowed) {
                val current = _walletState.value
                val cost = 500
                if (current.coins < cost) {
                    return@withContext Pair(false, "Insufficient coins. Costs 500 coins, you have ${current.coins}.")
                }
                val newCoins = current.coins - cost
                val newFreezes = current.streakFreezes + 1
                val newTx = WalletTransactionEntity(
                    type = "DEBIT",
                    amount = -cost,
                    description = "Purchased Streak Shield (Sandbox Mode)",
                    timestamp = System.currentTimeMillis()
                )
                _walletState.value = current.copy(
                    coins = newCoins,
                    streakFreezes = newFreezes,
                    transactions = listOf(newTx) + current.transactions
                )
                return@withContext Pair(true, "Streak Shield purchased successfully! (Sandbox Mode)")
            }
            try {
                val apiService = RetrofitClient.createApiService(_baseUrl.value, sessionManager)
                val res = apiService.postWalletAction(ApiWalletPostRequest(action = "purchase-streak-freeze"))
                if (res.isSuccessful && res.body()?.success == true) {
                    fetchWalletFromApi(apiService)
                    Pair(true, res.body()?.message ?: "Streak Shield purchased successfully!")
                } else {
                    Pair(false, res.body()?.message ?: "Failed to purchase Streak Shield")
                }
            } catch (e: Exception) {
                Pair(false, e.localizedMessage ?: "Network error occurred")
            }
        }
    }
}

