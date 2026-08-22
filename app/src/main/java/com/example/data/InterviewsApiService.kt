package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class InterviewItem(
    val id: String,
    val companyName: String,
    val jobTitle: String,
    val status: String, // "Upcoming", "Completed", "Cancelled"
    val date: String,
    val time: String,
    val location: String,
    val interviewer: String,
    val notes: String,
    val type: String = "JOB_TRACKER",
    val tenantId: String = "platform"
)

data class InterviewStatusUpdate(
    val id: String,
    val interviewId: String,
    val companyName: String,
    val jobTitle: String,
    val previousStatus: String,
    val newStatus: String, // "Upcoming", "Completed", "Cancelled", "Scheduled", "Feedback Added"
    val updateMessage: String,
    val timestamp: String,
    val iconType: String = "UPDATE"
)

data class InterviewsResponse(
    val status: String = "success",
    val endpoint: String = "/api/interviews",
    val totalInterviews: Int = 0,
    val upcomingCount: Int = 0,
    val completedCount: Int = 0,
    val cancelledCount: Int = 0,
    val interviews: List<InterviewItem> = emptyList(),
    val statusUpdates: List<InterviewStatusUpdate> = emptyList(),
    val isSampleData: Boolean = false,
    val sampleDataWarning: String? = null,
    val page: Int = 1,
    val limit: Int = 20,
    val total: Int = 0,
    val totalPages: Int = 0
)

sealed class InterviewDashboardUiState {
    object Loading : InterviewDashboardUiState()
    data class Success(val data: InterviewsResponse) : InterviewDashboardUiState()
    data class Error(val message: String, val fallback: InterviewsResponse? = null) : InterviewDashboardUiState()
}

interface InterviewsApi {
    @GET("api/interviews")
    suspend fun getInterviews(
        @retrofit2.http.Query("page") page: Int = 1,
        @retrofit2.http.Query("limit") limit: Int = 20
    ): Response<InterviewsBackendResponse>
}

@JsonClass(generateAdapter = true)
data class LiveParticipantDto(
    @param:Json(name = "name") val name: String? = null,
    @param:Json(name = "role") val role: String? = null,
    @param:Json(name = "userId") val userId: String? = null
)

@JsonClass(generateAdapter = true)
data class LiveInterviewDataDto(
    @param:Json(name = "description") val description: String? = null,
    @param:Json(name = "scheduledTime") val scheduledTime: String? = null,
    @param:Json(name = "confirmationStatus") val confirmationStatus: String? = null,
    @param:Json(name = "participants") val participants: List<LiveParticipantDto>? = null
)

@JsonClass(generateAdapter = true)
data class InterviewBackendDto(
    @param:Json(name = "id") val id: String? = null,
    @param:Json(name = "_id") val oid: String? = null,
    @param:Json(name = "companyName") val companyName: String? = null,
    @param:Json(name = "company") val company: String? = null,
    @param:Json(name = "company_name") val companyNameSnake: String? = null,
    @param:Json(name = "jobTitle") val jobTitle: String? = null,
    @param:Json(name = "role") val role: String? = null,
    @param:Json(name = "job_title") val jobTitleSnake: String? = null,
    @param:Json(name = "position") val position: String? = null,
    @param:Json(name = "status") val status: String? = null,
    @param:Json(name = "date") val date: String? = null,
    @param:Json(name = "scheduledDate") val scheduledDate: String? = null,
    @param:Json(name = "interviewDate") val interviewDate: String? = null,
    @param:Json(name = "time") val time: String? = null,
    @param:Json(name = "scheduledTime") val scheduledTime: String? = null,
    @param:Json(name = "interviewTime") val interviewTime: String? = null,
    @param:Json(name = "location") val location: String? = null,
    @param:Json(name = "venue") val venue: String? = null,
    @param:Json(name = "mode") val mode: String? = null,
    @param:Json(name = "interviewer") val interviewer: String? = null,
    @param:Json(name = "interviewerName") val interviewerName: String? = null,
    @param:Json(name = "panel") val panel: String? = null,
    @param:Json(name = "notes") val notes: String? = null,
    @param:Json(name = "description") val description: String? = null,
    @param:Json(name = "tenantId") val tenantId: String? = null,
    @param:Json(name = "tenant_id") val tenantIdSnake: String? = null,
    @param:Json(name = "topic") val topic: String? = null,
    @param:Json(name = "liveInterviewData") val liveInterviewData: LiveInterviewDataDto? = null
)

private fun formatIsoDateTime(isoString: String?): Pair<String, String>? {
    if (isoString.isNullOrBlank()) return null
    return try {
        val datePart = isoString.substringBefore("T")
        val timePart = isoString.substringAfter("T").substringBefore(".")
        
        val dateSplit = datePart.split("-")
        if (dateSplit.size != 3) return null
        val year = dateSplit[0]
        val monthNum = dateSplit[1].toIntOrNull() ?: 1
        val day = dateSplit[2].toIntOrNull() ?: 1
        
        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val monthName = months.getOrNull(monthNum - 1) ?: "July"
        val formattedDate = "$monthName $day, $year"
        
        val timeSplit = timePart.split(":")
        val formattedTime = if (timeSplit.size >= 2) {
            val hour = timeSplit[0].toIntOrNull() ?: 12
            val min = timeSplit[1]
            val ampm = if (hour >= 12) "PM" else "AM"
            val displayHour = if (hour % 12 == 0) 12 else hour % 12
            "$displayHour:$min $ampm"
        } else {
            "TBD"
        }
        
        Pair(formattedDate, formattedTime)
    } catch (e: Exception) {
        null
    }
}

private fun InterviewBackendDto.toUiInterviewItem(defaultTenantId: String = "platform"): InterviewItem {
    val resolvedId = (id ?: oid).takeIf { !it.isNullOrBlank() } ?: "int-${UUID.randomUUID().toString().take(8)}"
    val parsedDateTime = formatIsoDateTime(liveInterviewData?.scheduledTime)
    val resolvedDate = sequenceOf(date, scheduledDate, interviewDate, parsedDateTime?.first).firstOrNull { !it.isNullOrBlank() } ?: "TBD"
    val resolvedTime = sequenceOf(time, scheduledTime, interviewTime, parsedDateTime?.second).firstOrNull { !it.isNullOrBlank() } ?: "TBD"
    val resolvedJobTitle = sequenceOf(jobTitle, role, jobTitleSnake, position, topic).firstOrNull { !it.isNullOrBlank() } ?: "Interview"
    
    val resolvedCompany = sequenceOf(companyName, company, companyNameSnake).firstOrNull { !it.isNullOrBlank() } ?: run {
        if (resolvedJobTitle.contains("Expert", ignoreCase = true)) "Expert Coaching"
        else if (resolvedJobTitle.contains("AI", ignoreCase = true) || resolvedJobTitle.contains("Gemini", ignoreCase = true)) "AI Practice"
        else if (resolvedJobTitle.contains("Friend", ignoreCase = true) || resolvedJobTitle.contains("Peer", ignoreCase = true)) "Peer Practice"
        else "JobTraq Practice"
    }
    
    val resolvedStatus = sequenceOf(status).firstOrNull { !it.isNullOrBlank() } ?: "Upcoming"
    val resolvedInterviewer = sequenceOf(
        interviewer, 
        interviewerName, 
        panel, 
        liveInterviewData?.participants?.firstOrNull { it?.role?.lowercase() == "interviewer" }?.name
    ).firstOrNull { !it.isNullOrBlank() } ?: "TBD"
    
    val resolvedType = when {
        resolvedJobTitle.contains("Quiz", ignoreCase = true) || resolvedCompany.contains("Quiz", ignoreCase = true) -> "QUIZ"
        resolvedJobTitle.contains("Expert", ignoreCase = true) || resolvedCompany.contains("Expert", ignoreCase = true) || resolvedInterviewer.contains("Expert", ignoreCase = true) -> "EXPERT"
        resolvedJobTitle.contains("AI", ignoreCase = true) || resolvedJobTitle.contains("Gemini", ignoreCase = true) || resolvedCompany.contains("AI", ignoreCase = true) -> "AI_MOCK"
        resolvedJobTitle.contains("Friend", ignoreCase = true) || resolvedJobTitle.contains("Peer", ignoreCase = true) || resolvedCompany.contains("Peer", ignoreCase = true) -> "FRIEND"
        else -> "JOB_TRACKER"
    }

    val resolvedLocation = sequenceOf(location, venue, mode).firstOrNull { !it.isNullOrBlank() } ?: when (resolvedType) {
        "AI_MOCK" -> "AI Coach (Virtual)"
        "JOB_TRACKER", "EXPERT", "FRIEND" -> "Google Meet / Zoom"
        else -> "TBD"
    }

    val resolvedNotes = sequenceOf(notes, description).firstOrNull { !it.isNullOrBlank() } ?: ""
    val resolvedTenant = sequenceOf(tenantId, tenantIdSnake).firstOrNull { !it.isNullOrBlank() } ?: defaultTenantId
    
    return InterviewItem(
        id = resolvedId,
        companyName = resolvedCompany,
        jobTitle = resolvedJobTitle,
        status = when (resolvedStatus.trim().lowercase()) {
            "upcoming", "scheduled", "pending", "invited", "to_do" -> "Upcoming"
            "completed", "done", "finished", "passed", "closed_won" -> "Completed"
            "cancelled", "canceled", "rejected", "no_show", "withdrawn", "closed_lost" -> "Cancelled"
            else -> resolvedStatus.trim().ifBlank { "Upcoming" }
        },
        date = resolvedDate,
        time = resolvedTime,
        location = resolvedLocation,
        interviewer = resolvedInterviewer,
        notes = resolvedNotes,
        type = resolvedType,
        tenantId = resolvedTenant
    )
}

@JsonClass(generateAdapter = true)
data class InterviewsBackendResponse(
    @param:Json(name = "data") val data: List<InterviewBackendDto>? = null,
    @param:Json(name = "total") val total: Int? = null,
    @param:Json(name = "page") val page: Int? = null,
    @param:Json(name = "limit") val limit: Int? = null,
    @param:Json(name = "totalPages") val totalPages: Int? = null,
    @param:Json(name = "success") val success: Boolean? = null,
    @param:Json(name = "message") val message: String? = null
) {
    val totalOrZero: Int get() = total ?: 0
    val pageOrOne: Int get() = page ?: 1
    val limitOrTwenty: Int get() = limit ?: 20
    val totalPagesOrZero: Int get() = totalPages ?: 0
    val dataOrEmpty: List<InterviewBackendDto> get() = data ?: emptyList()
}

class InterviewsRepository(
    private val baseUrl: String = AppEnvironment.DEV.defaultBaseUrl,
    private val isDummyDataAllowed: Boolean = AppEnvironment.DEV.isDummyDataAllowed,
    private val sessionManager: SessionManager? = null,
    private val interviewDao: InterviewDao? = null,
    private val initialStatusUpdates: List<InterviewStatusUpdate> = emptyList()
) {

    private val _uiState = MutableStateFlow<InterviewDashboardUiState>(InterviewDashboardUiState.Loading)
    val uiState: StateFlow<InterviewDashboardUiState> = _uiState.asStateFlow()

    private val _lastRefreshed = MutableStateFlow<String>("Just now")
    val lastRefreshed: StateFlow<String> = _lastRefreshed.asStateFlow()

    init {
        interviewDao?.let { dao ->
            CoroutineScope(Dispatchers.IO).launch {
                val tenantFlow = if (sessionManager != null) {
                    sessionManager.sessionFlow.map { it.tenantId }.distinctUntilChanged()
                } else {
                    flowOf("platform")
                }
                
                @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
                tenantFlow.flatMapLatest { tenantId ->
                    dao.getInterviewsFlow(tenantId)
                }.collect { entities ->
                    val items = entities.map { it.toInterviewItem() }
                    val up = items.count { it.status.equals("Upcoming", ignoreCase = true) }
                    val done = items.count { it.status.equals("Completed", ignoreCase = true) }
                    val cancelled = items.count { it.status.equals("Cancelled", ignoreCase = true) }
                    
                    val response = InterviewsResponse(
                        status = "success",
                        endpoint = "/api/interviews",
                        totalInterviews = items.size,
                        upcomingCount = up,
                        completedCount = done,
                        cancelledCount = cancelled,
                        interviews = items,
                        statusUpdates = initialStatusUpdates,
                        page = 1,
                        limit = items.size.coerceAtLeast(20),
                        total = items.size,
                        totalPages = 1
                    )
                    _uiState.value = InterviewDashboardUiState.Success(normalizeInterviewsResponse(response))
                }
            }
        }
    }

    suspend fun schedulePracticeInterview(interview: InterviewEntity) {
        interviewDao?.insertOrUpdate(interview)
    }

    suspend fun deleteInterview(id: String) {
        interviewDao?.deleteById(id)
    }

    fun getDummyInterviewsResponse(): InterviewsResponse {
        val sampleInterviews = listOf(
            InterviewItem(
                id = "int-101",
                companyName = "TechCorp Inc",
                jobTitle = "Senior Frontend Engineer",
                status = "Upcoming",
                date = "Fri, July 25, 2026",
                time = "2:00 PM PST",
                location = "Google Meet / Remote",
                interviewer = "Sarah Jenkins (Lead Engineer)",
                notes = "System Design & Jetpack Compose state architecture discussion."
            ),
            InterviewItem(
                id = "int-102",
                companyName = "NextGen Solutions",
                jobTitle = "Android Mobile Specialist",
                status = "Upcoming",
                date = "Mon, July 28, 2026",
                time = "10:00 AM EST",
                location = "Zoom Meeting",
                interviewer = "David Miller (Engineering Manager)",
                notes = "Live Coding session on Kotlin Coroutines & Room Database."
            ),
            InterviewItem(
                id = "int-103",
                companyName = "Starlight AI",
                jobTitle = "AI Product Engineer",
                status = "Completed",
                date = "Wed, July 16, 2026",
                time = "11:30 AM PST",
                location = "San Francisco HQ",
                interviewer = "Elena Rostova (VP of AI)",
                notes = "Final round behavioral and technical alignment. Offer extended!"
            ),
            InterviewItem(
                id = "int-104",
                companyName = "CloudScale Systems",
                jobTitle = "Backend Kotlin Engineer",
                status = "Completed",
                date = "Mon, July 14, 2026",
                time = "3:00 PM PST",
                location = "Microsoft Teams",
                interviewer = "Marcus Vance (Principal Architect)",
                notes = "Passed initial round. Awaiting final offer packet."
            ),
            InterviewItem(
                id = "int-105",
                companyName = "Apex Dynamics",
                jobTitle = "Lead Mobile Architect",
                status = "Cancelled",
                date = "Tue, July 21, 2026",
                time = "1:00 PM CST",
                location = "Phone Screen",
                interviewer = "Recruiting Team",
                notes = "Position closed as role filled internally by client."
            ),
            InterviewItem(
                id = "int-106",
                companyName = "Global Cloud Networks",
                jobTitle = "Staff Android Developer",
                status = "Cancelled",
                date = "Thu, July 17, 2026",
                time = "4:00 PM EST",
                location = "Google Meet",
                interviewer = "Alex Mercer (Hiring Manager)",
                notes = "Candidate cancelled interview due to competing offer acceptance."
            )
        )

        val sampleUpdates = listOf(
            InterviewStatusUpdate(
                id = "upd-1",
                interviewId = "int-101",
                companyName = "TechCorp Inc",
                jobTitle = "Senior Frontend Engineer",
                previousStatus = "Scheduled",
                newStatus = "Upcoming",
                updateMessage = "Technical round confirmed. Calendar invite & Google Meet link attached.",
                timestamp = "Today at 09:15 AM",
                iconType = "SCHEDULED"
            ),
            InterviewStatusUpdate(
                id = "upd-2",
                interviewId = "int-105",
                companyName = "Apex Dynamics",
                jobTitle = "Lead Mobile Architect",
                previousStatus = "Upcoming",
                newStatus = "Cancelled",
                updateMessage = "Interview cancelled by recruiter. Reason: Position filled internally.",
                timestamp = "Yesterday at 4:45 PM",
                iconType = "CANCELLED"
            ),
            InterviewStatusUpdate(
                id = "upd-3",
                interviewId = "int-103",
                companyName = "Starlight AI",
                jobTitle = "AI Product Engineer",
                previousStatus = "Upcoming",
                newStatus = "Completed",
                updateMessage = "Final interview round completed! Recruiter provided positive rating score.",
                timestamp = "July 16, 2026 at 1:00 PM",
                iconType = "COMPLETED"
            ),
            InterviewStatusUpdate(
                id = "upd-4",
                interviewId = "int-102",
                companyName = "NextGen Solutions",
                jobTitle = "Android Mobile Specialist",
                previousStatus = "Applied",
                newStatus = "Upcoming",
                updateMessage = "Recruiter scheduled Round 2 Live Coding for July 28.",
                timestamp = "July 15, 2026 at 11:20 AM",
                iconType = "SCHEDULED"
            ),
            InterviewStatusUpdate(
                id = "upd-5",
                interviewId = "int-106",
                companyName = "Global Cloud Networks",
                jobTitle = "Staff Android Developer",
                previousStatus = "Upcoming",
                newStatus = "Cancelled",
                updateMessage = "Withdrawal notice sent by candidate after accepting Starlight AI offer.",
                timestamp = "July 14, 2026 at 5:10 PM",
                iconType = "CANCELLED"
            ),
            InterviewStatusUpdate(
                id = "upd-6",
                interviewId = "int-104",
                companyName = "CloudScale Systems",
                jobTitle = "Backend Kotlin Engineer",
                previousStatus = "Interviewing",
                newStatus = "Completed",
                updateMessage = "Architecture round marked as Completed. Final decision pending.",
                timestamp = "July 14, 2026 at 4:00 PM",
                iconType = "COMPLETED"
            )
        )

        val upcoming = sampleInterviews.count { it.status.equals("Upcoming", ignoreCase = true) }
        val completed = sampleInterviews.count { it.status.equals("Completed", ignoreCase = true) }
        val cancelled = sampleInterviews.count { it.status.equals("Cancelled", ignoreCase = true) }

        return InterviewsResponse(
            totalInterviews = sampleInterviews.size,
            upcomingCount = upcoming,
            completedCount = completed,
            cancelledCount = cancelled,
            interviews = sampleInterviews,
            statusUpdates = sampleUpdates
        )
    }

    private fun sanitizeApiError(rawError: String?, responseCode: Int, baseUrl: String, fallbackContext: String): String {
        val trimmed = (rawError ?: "").trim()
        val upper = trimmed.uppercase()
        val first200 = if (trimmed.length > 200) trimmed.substring(0, 200) else trimmed

        val looksLikeHtml = upper.startsWith("<!DOCTYPE") ||
            upper.startsWith("<HTML") ||
            upper.contains("<HEAD") ||
            upper.contains("<BODY") ||
            upper.contains("<TITLE") ||
            upper.contains("<NEXT/") ||
            upper.contains("_NEXT/STATIC/") ||
            upper.contains("<SCRIPT") ||
            upper.contains("<LINK REL=")

        val looksLikeWebServer404 = responseCode in 400..499 && (looksLikeHtml || first200.contains("route does not match", ignoreCase = true) || first200.contains("not found", ignoreCase = true))

        return when {
            looksLikeWebServer404 -> "No API endpoint at $baseUrl. Got HTTP $responseCode web/HTML response — $baseUrl looks like a website, not a JSON API server. Please check your Base URL in Settings and use a real API server (e.g. your localhost backend or JobTraq PROD API URL)."
            looksLikeHtml -> "Unexpected HTML response from $baseUrl (HTTP $responseCode). This URL returned a web page instead of JSON. Verify the Base URL points to an API server, not a frontend website."
            trimmed.isBlank() -> "API Error (HTTP $responseCode): Failed to $fallbackContext. No error details returned by server."
            trimmed.length > 300 -> "API Error (HTTP $responseCode): $first200… — check server logs for full response."
            else -> "API Error (HTTP $responseCode): $trimmed"
        }
    }

    private fun sanitizeExceptionError(e: kotlin.Exception, baseUrl: String): String {
        val msg = (e.localizedMessage ?: e.message ?: "").trim()
        val causeChain = generateSequence(e.cause) { it.cause }.mapNotNull { it.localizedMessage ?: it.message }.joinToString(" → ")

        val combined = (msg + " " + causeChain).lowercase()
        val first180Msg = if (msg.length > 180) msg.substring(0, 180) + "…" else msg

        return when {
            combined.contains("failed to connect") ||
                combined.contains("connection refused") ||
                combined.contains("etimedout") ||
                combined.contains("econnrefused") ||
                combined.contains("unknownhost") -> {
                val hint = if (baseUrl.contains("localhost", ignoreCase = true) || baseUrl.contains("127.0.0.1")) {
                    " On Android use http://10.0.2.2:PORT for emulator or your PC's LAN IP for a physical device."
                } else ""
                "Could not reach $baseUrl — server is not running, unreachable, or wrong URL used.$hint"
            }
            combined.contains("cletext") || combined.contains("cleartext") || combined.contains("http traffic") ->
                "Cleartext HTTP blocked for $baseUrl. Use https:// or enable android:usesCleartextTraffic=\"true\" (it is already enabled in this app)."
            combined.contains("ssl") || combined.contains("certpath") || combined.contains("handshake") ->
                "TLS/SSL error connecting to $baseUrl. Check that the URL uses a valid HTTPS certificate."
            msg.isBlank() -> "Unknown network error while calling $baseUrl."
            else -> "API connection failed to $baseUrl: $first180Msg"
        }
    }

    suspend fun fetchInterviewsFromApi(page: Int = 1, limit: Int = 20): InterviewsResponse = withContext(Dispatchers.IO) {
        _uiState.value = InterviewDashboardUiState.Loading

        val resolvedTenantId: String = runCatching {
            sessionManager?.currentSession()?.tenantId?.takeIf { it.isNotBlank() }
        }.getOrNull() ?: "platform"

        var errorMessage: String? = null

        if (baseUrl.isNotBlank()) {
            val urlsToTry = mutableListOf(baseUrl)
            val alt = BaseUrlResolver.alternateJobtraqHostForDnsFallback(baseUrl)
            if (alt != null) urlsToTry.add(alt)
            var lastEx: Exception? = null

            for (candidateUrl in urlsToTry) {
                try {
                    val withScheme = when {
                        candidateUrl.startsWith("http://", ignoreCase = true) ||
                            candidateUrl.startsWith("https://", ignoreCase = true) -> candidateUrl
                        else -> "https://$candidateUrl"
                    }
                    val cleanUrl = if (withScheme.endsWith("/")) withScheme else "$withScheme/"

                    val moshi = Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()
                    val okHttpBuilder = OkHttpClient.Builder()
                        .connectTimeout(8, TimeUnit.SECONDS)
                        .readTimeout(8, TimeUnit.SECONDS)
                        .writeTimeout(8, TimeUnit.SECONDS)
                    if (sessionManager != null) {
                        okHttpBuilder.addInterceptor(AuthInterceptor(sessionManager))
                    }
                    val okHttpClient = okHttpBuilder.build()
                    val interviewsApi: InterviewsApi = Retrofit.Builder()
                        .baseUrl(cleanUrl)
                        .client(okHttpClient)
                        .addConverterFactory(MoshiConverterFactory.create(moshi))
                        .build()
                        .create(InterviewsApi::class.java)

                    val httpCode: Int
                    var rawError: String? = null
                    val backend: InterviewsBackendResponse? = try {
                        val direct = interviewsApi.getInterviews(page = page, limit = limit)
                        httpCode = direct.code()
                        if (!direct.isSuccessful) rawError = runCatching { direct.errorBody()?.string() }.getOrNull()
                        direct.body()
                    } catch (e: kotlin.Exception) {
                        throw e
                    }

                    if (backend != null) {
                        val dataItems: List<InterviewItem> = backend.dataOrEmpty.map { it.toUiInterviewItem(defaultTenantId = resolvedTenantId) }
                        val totalApi = backend.totalOrZero
                        val pageApi = backend.pageOrOne
                        val limitApi = backend.limitOrTwenty
                        val totalPagesApi = backend.totalPagesOrZero
                        val up = dataItems.count { it.status.equals("Upcoming", ignoreCase = true) }
                        val done = dataItems.count { it.status.equals("Completed", ignoreCase = true) }
                        val cancelled = dataItems.count { it.status.equals("Cancelled", ignoreCase = true) }
                        val finalResponse = InterviewsResponse(
                            status = if (backend.success == false && !backend.message.isNullOrBlank()) "error" else "success",
                            endpoint = "/api/interviews",
                            totalInterviews = totalApi.coerceAtLeast(dataItems.size),
                            upcomingCount = up,
                            completedCount = done,
                            cancelledCount = cancelled,
                            interviews = dataItems,
                            statusUpdates = initialStatusUpdates,
                            page = pageApi,
                            limit = limitApi,
                            total = totalApi,
                            totalPages = totalPagesApi
                        )
                        val normalized = normalizeInterviewsResponse(finalResponse)
                        
                        // Save job tracker and practice interviews to Room database
                        interviewDao?.let { dao ->
                            // 1. Delete all JOB_TRACKER interviews since they always sync from backend
                            dao.deleteByType(resolvedTenantId, "JOB_TRACKER")
                            
                            // 2. For other types, delete only the ones that were previously synced from backend (whose IDs do NOT start with ai-, exp-, frd-)
                            val currentDb = dao.getInterviews(resolvedTenantId)
                            val syncedIdsToDelete = currentDb.filter { dbEnt ->
                                val isSyncedType = dbEnt.type == "AI_MOCK" || dbEnt.type == "EXPERT" || dbEnt.type == "FRIEND"
                                val isNotLocalId = !dbEnt.id.startsWith("ai-") && !dbEnt.id.startsWith("exp-") && !dbEnt.id.startsWith("frd-")
                                isSyncedType && isNotLocalId
                            }.map { it.id }
                            syncedIdsToDelete.forEach { id ->
                                dao.deleteById(id)
                            }
                            
                            // 3. Insert all new backend interviews using their resolved types
                            dao.insertAll(dataItems.map { it.toInterviewEntity() })
                        } ?: run {
                            _uiState.value = InterviewDashboardUiState.Success(normalized)
                        }

                        _lastRefreshed.value = "Just now"
                        return@withContext normalized
                    } else {
                        errorMessage = sanitizeApiError(
                            rawError = rawError,
                            responseCode = httpCode,
                            baseUrl = candidateUrl,
                            fallbackContext = "fetch interviews"
                        )
                        break
                    }
                } catch (e: kotlin.Exception) {
                    lastEx = e
                    if (!BaseUrlResolver.isDnsFailure(e)) {
                        errorMessage = sanitizeExceptionError(e, candidateUrl)
                        break
                    }
                }
            }
            if (errorMessage == null && lastEx != null) {
                errorMessage = sanitizeExceptionError(lastEx, baseUrl)
            }
        } else {
            errorMessage = "No base URL configured."
        }

        val suggestion = if (baseUrl.isBlank()) {
            " Pick JobTraq PROD preset or configure a valid API server URL in Settings → Base URL."
        } else {
            val host = baseUrl.trim().lowercase()
                .removePrefix("http://").removePrefix("https://").substringBefore("/").substringBefore(":")
            if (host == "localhost" || host == "127.0.0.1") {
                " Your Base URL uses localhost which is unreachable on Android. In Settings → Base URL, save 10.0.2.2:PORT instead."
            } else if (host.endsWith(".jobtraq.in") || host == "jobtraq.in" || host == "www.jobtraq.in") {
                " (DNS unreachable on this device — verify network connection and retry.)"
            } else {
                " Pick JobTraq PROD preset or configure a valid API server URL in Settings → Base URL."
            }
        }

        val warning = (errorMessage ?: "Interviews API unavailable") + suggestion

        delay(300)
        
        val fallback = if (interviewDao != null) {
            _lastRefreshed.value = "Offline (cached data)"
            val resolvedTenantId = runCatching {
                sessionManager?.currentSession()?.tenantId?.takeIf { it.isNotBlank() }
            }.getOrNull() ?: "platform"
            
            val dbEntitiesList = try {
                interviewDao.getInterviews(resolvedTenantId)
            } catch (e: Exception) {
                emptyList()
            }
            
            val items = dbEntitiesList.map { it.toInterviewItem() }
            val up = items.count { it.status.equals("Upcoming", ignoreCase = true) }
            val done = items.count { it.status.equals("Completed", ignoreCase = true) }
            val cancelled = items.count { it.status.equals("Cancelled", ignoreCase = true) }
            
            val res = InterviewsResponse(
                status = "success",
                endpoint = "/api/interviews",
                totalInterviews = items.size,
                upcomingCount = up,
                completedCount = done,
                cancelledCount = cancelled,
                interviews = items,
                statusUpdates = initialStatusUpdates,
                isSampleData = false,
                sampleDataWarning = warning
            )
            val normalized = normalizeInterviewsResponse(res)
            _uiState.value = InterviewDashboardUiState.Success(normalized)
            normalized
        } else {
            val dummy = getDummyInterviewsResponse().copy(
                isSampleData = true,
                sampleDataWarning = warning
            )
            _uiState.value = InterviewDashboardUiState.Success(dummy)
            _lastRefreshed.value = "Just now (sample data)"
            dummy
        }
        fallback
    }
}

private fun normalizeInterviewsResponse(response: InterviewsResponse): InterviewsResponse {
    val nonQuizInterviews = response.interviews.filter {
        it.type != "QUIZ" &&
        !it.jobTitle.contains("quiz", ignoreCase = true) &&
        !it.companyName.contains("quiz", ignoreCase = true)
    }
    val up = nonQuizInterviews.count {
        it.status.equals("Upcoming", ignoreCase = true) || it.status.equals("Scheduled", ignoreCase = true)
    }
    val done = nonQuizInterviews.count {
        it.status.equals("Completed", ignoreCase = true) || it.status.equals("Past", ignoreCase = true)
    }
    val cancelled = nonQuizInterviews.count {
        it.status.equals("Cancelled", ignoreCase = true)
    }
    return response.copy(
        totalInterviews = nonQuizInterviews.size,
        upcomingCount = up,
        completedCount = done,
        cancelledCount = cancelled,
        interviews = nonQuizInterviews,
        endpoint = response.endpoint.ifBlank { "/api/interviews" },
        status = response.status.ifBlank { "success" }
    )
}
