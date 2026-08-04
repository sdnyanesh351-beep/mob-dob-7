package com.example.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ApiDashboardSummaryResponse
import com.example.data.ApiErrorSanitizer
import com.example.data.ApiLevelProgressDto
import com.example.data.AppEnvironment
import com.example.data.AuthDatabase
import com.example.data.AuthRepository
import com.example.data.BaseUrlResolver
import com.example.data.InterviewDashboardUiState
import com.example.data.InterviewsResponse
import com.example.data.InterviewsRepository
import com.example.data.RetrofitClient
import com.example.data.SessionManager
import com.example.data.StreakData
import com.example.data.StreakDataStoreManager
import com.example.data.UserEntity
import com.example.data.deriveLevelProgress
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import retrofit2.HttpException

enum class AuthScreenMode {
    SPLASH,
    ONBOARDING,
    WELCOME,
    LOGIN,
    SIGNUP,
    FORGOT_PASSWORD,
    BASE_URL_CONFIG,
    POST_LOGIN_PROGRESS,
    LOGGED_IN
}

data class PasswordStrength(
    val score: Int = 0,
    val hasMinLength: Boolean = false,
    val hasUppercase: Boolean = false,
    val hasDigit: Boolean = false,
    val hasSpecialChar: Boolean = false
) {
    val label: String
        get() = when (score) {
            0 -> "Very Weak"
            1 -> "Weak"
            2 -> "Fair"
            3 -> "Strong"
            4 -> "Very Strong"
            else -> "Weak"
        }
}

data class AuthUiState(
    val currentMode: AuthScreenMode = AuthScreenMode.SPLASH,
    val isLoading: Boolean = false,
    val isAuthRedirecting: Boolean = false,
    val authRedirectMessage: String = "",
    val selectedTenant: String = "platform",
    val activeEnvironment: AppEnvironment = AppEnvironment.DEV,
    val baseUrl: String = AppEnvironment.DEV.defaultBaseUrl,
    val tenantSearchQuery: String = "",
    val loggedInUser: UserEntity? = null,
    val messageSnackbar: String? = null,

    val loginEmail: String = "alice@jobtraq.in",
    val loginPassword: String = "password123",
    val isLoginPasswordVisible: Boolean = false,
    val isRememberMeChecked: Boolean = true,
    val loginError: String? = null,

    val signupName: String = "",
    val signupEmail: String = "",
    val signupPhone: String = "",
    val signupReferralCode: String = "",
    val signupPassword: String = "",
    val signupConfirmPassword: String = "",
    val isSignupPasswordVisible: Boolean = false,
    val isSignupConfirmPasswordVisible: Boolean = false,
    val isTermsAccepted: Boolean = false,
    val selectedAvatarIndex: Int = 0,
    val signupError: String? = null,
    val passwordStrength: PasswordStrength = PasswordStrength(),

    val resetEmail: String = "",
    val resetOtpCode: String = "",
    val resetNewPassword: String = "",
    val resetConfirmPassword: String = "",
    val resetStep: Int = 1,
    val resetError: String? = null,

    val isTermsModalOpen: Boolean = false,
    val isBiometricModalOpen: Boolean = false,
    val isEditProfileModalOpen: Boolean = false,
    val darkThemeOverride: Boolean = false,

    val authToken: String? = null,
    val postLoginProgress: ApiLevelProgressDto? = null,
    val isPostLoginLoadingProgress: Boolean = false,
    val postLoginProgressError: String? = null,
    val isPostLoginLoadingInterviews: Boolean = false,
    val postLoginInterviewsError: String? = null,
    val postLoginInterviews: InterviewsResponse? = null,

    val currentLocale: String = "en",
    val availableLocales: List<Pair<String, String>> = listOf(
        "en" to "English",
        "hi" to "हिन्दी (Hindi)",
        "mr" to "मराठी (Marathi)"
    ),
    val streakData: StreakData = StreakData()
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AuthRepository
    val sessionManager: SessionManager
    val streakDataStoreManager: StreakDataStoreManager
    private var heartbeatJob: Job? = null

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        val userDao = AuthDatabase.getDatabase(application).userDao()
        repository = AuthRepository(userDao)
        sessionManager = SessionManager(application.applicationContext)
        streakDataStoreManager = StreakDataStoreManager(application.applicationContext)

        viewModelScope.launch {
            streakDataStoreManager.streakDataFlow.collect { data ->
                _uiState.update { it.copy(streakData = data) }
            }
        }

        viewModelScope.launch {
            streakDataStoreManager.recordDailyLogin()
        }

        val prefs = application.getSharedPreferences("api_config_prefs", android.content.Context.MODE_PRIVATE)
        val savedEnvKey = prefs.getString("active_environment", "DEV") ?: "DEV"
        val savedEnv = AppEnvironment.fromKey(savedEnvKey)
        val savedBaseUrl = prefs.getString("base_url", savedEnv.defaultBaseUrl) ?: savedEnv.defaultBaseUrl

        _uiState.update {
            it.copy(
                activeEnvironment = savedEnv,
                baseUrl = savedBaseUrl
            )
        }

        viewModelScope.launch {
            repository.seedDemoUserIfEmpty()
        }

        viewModelScope.launch {
            val session = sessionManager.currentSession()
            _uiState.update { it.copy(currentLocale = session.locale, selectedTenant = session.tenantId) }
            if (session.rememberMe && session.userEmail?.isNotBlank() == true) {
                _uiState.update { it.copy(loginEmail = session.userEmail, isRememberMeChecked = true) }
            }
            if (session.authToken != null && session.userId != null) {
                val user = withContext(Dispatchers.IO) {
                    userDao.getUserById(session.userId)
                }
                if (user != null) {
                    _uiState.update {
                        it.copy(
                            currentMode = AuthScreenMode.LOGGED_IN,
                            loggedInUser = user,
                            authToken = session.authToken,
                            selectedTenant = session.tenantId,
                            messageSnackbar = "Welcome back, ${user.fullName}!"
                        )
                    }
                    startSessionHeartbeat()
                } else {
                    sessionManager.clearSession()
                }
            }
        }

        startSessionChangeListener()
    }

    private fun startSessionChangeListener() {
        sessionManager.sessionFlow
            .onEach { session ->
                if (_uiState.value.currentLocale != session.locale) {
                    _uiState.update { it.copy(currentLocale = session.locale) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun startSessionHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(60_000)
                val session = sessionManager.currentSession()
                if (session.authToken == null) {
                    heartbeatJob?.cancel()
                    break
                }
                val urlsToTry = mutableListOf(_uiState.value.baseUrl)
                val alt = BaseUrlResolver.alternateJobtraqHostForDnsFallback(_uiState.value.baseUrl)
                if (alt != null) urlsToTry.add(alt)
                var got401 = false
                for (candidateUrl in urlsToTry) {
                    try {
                        val api = RetrofitClient.createApiService(candidateUrl, sessionManager)
                        val response = api.getDashboardSummary()
                        if (response.code() == 401) {
                            got401 = true
                            break
                        }
                        break
                    } catch (e: Exception) {
                        val dns = BaseUrlResolver.isDnsFailure(e)
                        if (!dns) break
                    }
                }
                if (got401) {
                    withContext(Dispatchers.Main.immediate) {
                        _uiState.update {
                            it.copy(
                                messageSnackbar = "Session expired. Please log in again."
                            )
                        }
                        forceLogoutInternal()
                    }
                    break
                }
            }
        }
    }

    fun setLocale(locale: String) {
        viewModelScope.launch {
            sessionManager.setLocale(locale)
            _uiState.update {
                it.copy(
                    currentLocale = locale,
                    messageSnackbar = "Language updated"
                )
            }
        }
    }

    fun setEnvironment(env: AppEnvironment) {
        val prefs = getApplication<Application>().getSharedPreferences("api_config_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit()
            .putString("active_environment", env.keyName)
            .putString("base_url", env.defaultBaseUrl)
            .apply()
        _uiState.update {
            it.copy(
                activeEnvironment = env,
                baseUrl = env.defaultBaseUrl,
                messageSnackbar = "Switched to ${env.displayName} (${env.badgeTag})"
            )
        }
    }

    fun saveBaseUrl(url: String) {
        val cleanUrl = url.trim().removeSuffix("/")
        val prefs = getApplication<Application>().getSharedPreferences("api_config_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("base_url", cleanUrl).apply()
        _uiState.update {
            it.copy(
                baseUrl = cleanUrl,
                messageSnackbar = "API Base URL saved locally: $cleanUrl"
            )
        }
    }

    fun navigateTo(mode: AuthScreenMode) {
        _uiState.update {
            it.copy(
                currentMode = mode,
                loginError = null,
                signupError = null,
                resetError = null,
                messageSnackbar = null
            )
        }
    }

    fun onSelectTenant(tenant: String) {
        _uiState.update { it.copy(selectedTenant = tenant) }
        viewModelScope.launch { sessionManager.setTenantId(tenant) }
    }

    fun onTenantSearchQueryChanged(query: String) {
        _uiState.update { it.copy(tenantSearchQuery = query) }
    }

    fun onLoginEmailChanged(email: String) {
        _uiState.update { it.copy(loginEmail = email, loginError = null) }
    }

    fun onLoginPasswordChanged(password: String) {
        _uiState.update { it.copy(loginPassword = password, loginError = null) }
    }

    fun toggleLoginPasswordVisibility() {
        _uiState.update { it.copy(isLoginPasswordVisible = !it.isLoginPasswordVisible) }
    }

    fun toggleRememberMe(checked: Boolean) {
        _uiState.update { it.copy(isRememberMeChecked = checked) }
    }

    fun submitLogin() {
        val email = _uiState.value.loginEmail
        val password = _uiState.value.loginPassword
        val tenant = _uiState.value.selectedTenant

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(loginError = "Please enter both email and password.") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(loginError = "Please enter a valid email address.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isAuthRedirecting = true,
                    authRedirectMessage = "Authenticating & redirecting to ${tenant.uppercase()} tenant workspace...",
                    loginError = null,
                    authToken = null,
                    postLoginProgress = null,
                    isPostLoginLoadingProgress = false,
                    postLoginProgressError = null,
                    postLoginInterviews = null,
                    postLoginInterviewsError = null
                )
            }
            delay(1000)

            val baseUrl = _uiState.value.baseUrl
            val env = _uiState.value.activeEnvironment
            val result = repository.loginUser(
                email = email,
                passwordRaw = password,
                baseUrl = baseUrl,
                tenantId = tenant,
                isDummyDataAllowed = env.isDummyDataAllowed
            )
            result.onSuccess { loginResult ->
                val rememberMe = _uiState.value.isRememberMeChecked
                sessionManager.saveLoginSession(
                    token = loginResult.token,
                    email = loginResult.user.email,
                    userId = loginResult.user.id,
                    rememberMe = rememberMe,
                    tenantId = tenant
                )
                val offlineHint = if (loginResult.dnsUnreachableFallback) {
                    " (⚠️ ${baseUrl} DNS unreachable on this device — using local demo account)"
                } else ""
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthRedirecting = false,
                        loggedInUser = loginResult.user,
                        authToken = loginResult.token,
                        currentMode = AuthScreenMode.POST_LOGIN_PROGRESS,
                        isPostLoginLoadingProgress = true,
                        messageSnackbar = "Welcome back, ${loginResult.user.fullName}! (${tenant.uppercase()} Tenant)$offlineHint"
                    )
                }
                startSessionHeartbeat()
                fetchPostLoginDashboardAfterLogin(dnsAlreadyUnreachable = loginResult.dnsUnreachableFallback)
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthRedirecting = false,
                        loginError = exception.message ?: "Authentication failed."
                    )
                }
            }
        }
    }

    private suspend fun fetchPostLoginDashboardAfterLogin(dnsAlreadyUnreachable: Boolean = false) {
        val baseUrl = _uiState.value.baseUrl
        val isDummyAllowed = _uiState.value.activeEnvironment.isDummyDataAllowed

        fun defaultProgress(rawXp: Int = 850): ApiLevelProgressDto {
            val xpForNext = 1000
            return ApiLevelProgressDto(
                level = 1 + (rawXp / xpForNext),
                currentXp = rawXp % xpForNext,
                xpForNextLevel = xpForNext,
                rank = 42,
                percentToNext = ((rawXp % xpForNext).toFloat() / xpForNext) * 100f,
                dayStreak = 0,
                badgesEarned = 0,
                remainingXp = xpForNext - (rawXp % xpForNext)
            )
        }

        if (dnsAlreadyUnreachable) {
            val p = defaultProgress()
            _uiState.update {
                it.copy(
                    isPostLoginLoadingProgress = false,
                    postLoginProgress = p,
                    postLoginProgressError = null
                )
            }
            return
        }

        val urlsToTry = mutableListOf(baseUrl)
        val alt = BaseUrlResolver.alternateJobtraqHostForDnsFallback(baseUrl)
        if (alt != null) urlsToTry.add(alt)
        var allDnsFailed = true

        for (candidateUrl in urlsToTry) {
            try {
                val api = RetrofitClient.createApiService(candidateUrl, sessionManager)
                val response = api.getDashboardSummary()
                allDnsFailed = false
                val body = response.body()
                val progress: ApiLevelProgressDto = when {
                    response.isSuccessful && body?.success == true && body?.progress != null ->
                        body.progress!!

                    response.isSuccessful && body?.success == true && body?.data != null ->
                        body.data.deriveLevelProgress()

                    response.isSuccessful && body != null ->
                        body.data.deriveLevelProgress()

                    response.isSuccessful ->
                        defaultProgress()

                    isDummyAllowed -> defaultProgress()

                    else -> {
                        val rawError = ApiErrorSanitizer.safeRawError(response.errorBody())
                        val msg = ApiErrorSanitizer.sanitizeApiError(
                            rawError = rawError,
                            responseCode = response.code(),
                            baseUrl = candidateUrl,
                            fallbackContext = "fetch dashboard progress"
                        )
                        _uiState.update { it.copy(isPostLoginLoadingProgress = false, postLoginProgressError = msg) }
                        return
                    }
                }
                val enriched = progress.copy(
                    percentToNext = if (progress.percentToNext > 0f) progress.percentToNext else {
                        if (progress.xpForNextLevel > 0) (progress.currentXp.toFloat() / progress.xpForNextLevel) * 100f else 0f
                    },
                    remainingXp = if (progress.remainingXp > 0) progress.remainingXp else {
                        (progress.xpForNextLevel - progress.currentXp).coerceAtLeast(0)
                    }
                )
                _uiState.update {
                    it.copy(isPostLoginLoadingProgress = false, postLoginProgress = enriched, postLoginProgressError = null)
                }
                return
            } catch (e: kotlin.Exception) {
                val dns = BaseUrlResolver.isDnsFailure(e)
                if (!dns) {
                    allDnsFailed = false
                    if (isDummyAllowed) {
                        val p = defaultProgress()
                        _uiState.update {
                            it.copy(
                                isPostLoginLoadingProgress = false,
                                postLoginProgress = p,
                                postLoginProgressError = null
                            )
                        }
                    } else {
                        val msg = ApiErrorSanitizer.sanitizeExceptionError(e, candidateUrl)
                        _uiState.update { it.copy(isPostLoginLoadingProgress = false, postLoginProgressError = msg) }
                    }
                    return
                }
            }
        }

        if (allDnsFailed) {
            val p = defaultProgress()
            _uiState.update {
                it.copy(
                    isPostLoginLoadingProgress = false,
                    postLoginProgress = p,
                    postLoginProgressError = null
                )
            }
        }
    }

    fun continueFromPostLoginProgressToApp() {
        val user = _uiState.value.loggedInUser ?: run {
            _uiState.update { it.copy(currentMode = AuthScreenMode.LOGIN) }
            return
        }
        _uiState.update {
            it.copy(isPostLoginLoadingInterviews = true, postLoginInterviewsError = null, currentMode = AuthScreenMode.LOGGED_IN)
        }
    }

    fun continuePostLoginAndFetchInterviewsThenEnter(onFinished: () -> Unit = {}) {
        val user = _uiState.value.loggedInUser ?: run {
            _uiState.update { it.copy(currentMode = AuthScreenMode.LOGIN) }
            onFinished()
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isPostLoginLoadingInterviews = true, postLoginInterviewsError = null) }
            val baseUrl = _uiState.value.baseUrl
            val isDummyAllowed = _uiState.value.activeEnvironment.isDummyDataAllowed

            val urlsToTry = mutableListOf(baseUrl)
            val alt = BaseUrlResolver.alternateJobtraqHostForDnsFallback(baseUrl)
            if (alt != null) urlsToTry.add(alt)
            var allDnsFailed = true

            for (candidateUrl in urlsToTry) {
                val interviewsRepo = InterviewsRepository(baseUrl = candidateUrl, isDummyDataAllowed = true, sessionManager = sessionManager)
                try {
                    val data = interviewsRepo.fetchInterviewsFromApi()
                    allDnsFailed = false
                    _uiState.update {
                        it.copy(
                            postLoginInterviews = data,
                            isPostLoginLoadingInterviews = false,
                            postLoginInterviewsError = null,
                            currentMode = AuthScreenMode.LOGGED_IN
                        )
                    }
                    onFinished()
                    return@launch
                } catch (e: kotlin.Exception) {
                    val dns = BaseUrlResolver.isDnsFailure(e)
                    if (!dns) {
                        allDnsFailed = false
                        if (isDummyAllowed) {
                            val data = interviewsRepo.getDummyInterviewsResponse()
                            _uiState.update {
                                it.copy(
                                    postLoginInterviews = data,
                                    isPostLoginLoadingInterviews = false,
                                    postLoginInterviewsError = null,
                                    currentMode = AuthScreenMode.LOGGED_IN
                                )
                            }
                        } else {
                            val msg = ApiErrorSanitizer.sanitizeExceptionError(e, candidateUrl)
                            _uiState.update {
                                it.copy(
                                    isPostLoginLoadingInterviews = false,
                                    postLoginInterviewsError = msg,
                                    currentMode = AuthScreenMode.LOGGED_IN
                                )
                            }
                        }
                        onFinished()
                        return@launch
                    }
                }
            }

            // All DNS failed → show dummy interviews
            val interviewsRepo = InterviewsRepository(baseUrl = baseUrl, isDummyDataAllowed = true, sessionManager = sessionManager)
            val data = interviewsRepo.getDummyInterviewsResponse()
            _uiState.update {
                it.copy(
                    postLoginInterviews = data,
                    isPostLoginLoadingInterviews = false,
                    postLoginInterviewsError = null,
                    currentMode = AuthScreenMode.LOGGED_IN
                )
            }
            onFinished()
        }
    }

    fun fetchPostLoginInterviews() {
        continuePostLoginAndFetchInterviewsThenEnter()
    }

    fun refreshProgressForPostLogin() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPostLoginLoadingProgress = true,
                    postLoginProgressError = null
                )
            }
            fetchPostLoginDashboardAfterLogin()
        }
    }

    fun onSignupNameChanged(name: String) {
        _uiState.update { it.copy(signupName = name, signupError = null) }
    }

    fun onSignupEmailChanged(email: String) {
        _uiState.update { it.copy(signupEmail = email, signupError = null) }
    }

    fun onSignupPhoneChanged(phone: String) {
        _uiState.update { it.copy(signupPhone = phone) }
    }

    fun onSignupReferralCodeChanged(code: String) {
        _uiState.update { it.copy(signupReferralCode = code) }
    }

    fun onSignupPasswordChanged(password: String) {
        val strength = calculatePasswordStrength(password)
        _uiState.update {
            it.copy(
                signupPassword = password,
                passwordStrength = strength,
                signupError = null
            )
        }
    }

    fun onSignupConfirmPasswordChanged(password: String) {
        _uiState.update { it.copy(signupConfirmPassword = password, signupError = null) }
    }

    fun toggleSignupPasswordVisibility() {
        _uiState.update { it.copy(isSignupPasswordVisible = !it.isSignupPasswordVisible) }
    }

    fun toggleSignupConfirmPasswordVisibility() {
        _uiState.update { it.copy(isSignupConfirmPasswordVisible = !it.isSignupConfirmPasswordVisible) }
    }

    fun toggleTermsAccepted(accepted: Boolean) {
        _uiState.update { it.copy(isTermsAccepted = accepted) }
    }

    fun selectAvatarIndex(index: Int) {
        _uiState.update { it.copy(selectedAvatarIndex = index) }
    }

    fun submitSignup() {
        val state = _uiState.value
        if (state.signupName.isBlank()) {
            _uiState.update { it.copy(signupError = "Please enter your full name.") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.signupEmail).matches()) {
            _uiState.update { it.copy(signupError = "Please enter a valid email address.") }
            return
        }
        val password = state.signupPassword
        val hasUppercase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        if (password.length < 8 || !hasUppercase || !hasDigit || !hasSpecial) {
            _uiState.update { it.copy(signupError = "Password must be at least 8 characters and contain at least 1 uppercase letter, 1 digit, and 1 special character.") }
            return
        }
        if (state.signupPassword != state.signupConfirmPassword) {
            _uiState.update { it.copy(signupError = "Passwords do not match.") }
            return
        }
        if (!state.isTermsAccepted) {
            _uiState.update { it.copy(signupError = "You must accept the Terms and Conditions.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isAuthRedirecting = true,
                    authRedirectMessage = "Registering & redirecting to ${state.selectedTenant.uppercase()} tenant workspace...",
                    signupError = null
                )
            }
            delay(1000)

            val result = repository.registerUser(
                fullName = state.signupName,
                email = state.signupEmail,
                passwordRaw = state.signupPassword,
                phone = state.signupPhone,
                avatarBadgeIndex = state.selectedAvatarIndex,
                baseUrl = state.baseUrl,
                tenantId = state.selectedTenant,
                isDummyDataAllowed = state.activeEnvironment.isDummyDataAllowed
            )

            result.onSuccess { userId ->
                val user = repository.getUserByEmail(state.signupEmail)
                if (user != null) {
                    sessionManager.saveLoginSession(
                        token = null,
                        email = user.email,
                        userId = user.id,
                        rememberMe = true,
                        tenantId = state.selectedTenant
                    )
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthRedirecting = false,
                        loggedInUser = user,
                        currentMode = AuthScreenMode.LOGGED_IN,
                        messageSnackbar = "Account created successfully! (${state.selectedTenant.uppercase()} Tenant)"
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthRedirecting = false,
                        signupError = exception.message ?: "Signup failed."
                    )
                }
            }
        }
    }

    fun onResetEmailChanged(email: String) {
        _uiState.update { it.copy(resetEmail = email, resetError = null) }
    }

    fun onResetOtpChanged(otp: String) {
        _uiState.update { it.copy(resetOtpCode = otp, resetError = null) }
    }

    fun onResetNewPasswordChanged(password: String) {
        _uiState.update { it.copy(resetNewPassword = password, resetError = null) }
    }

    fun onResetConfirmPasswordChanged(password: String) {
        _uiState.update { it.copy(resetConfirmPassword = password, resetError = null) }
    }

    fun submitResetEmail() {
        val email = _uiState.value.resetEmail
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(resetError = "Please enter a valid email address.") }
            return
        }

        val isDummyAllowed = _uiState.value.activeEnvironment.isDummyDataAllowed
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, resetError = null) }
            delay(600)

            if (isDummyAllowed) {
                val user = repository.getUserByEmail(email)
                if (user == null) {
                    _uiState.update {
                        it.copy(isLoading = false, resetError = "No account registered with this email.")
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            resetStep = 2,
                            messageSnackbar = "Verification code sent to $email"
                        )
                    }
                }
            } else {
                val baseUrl = _uiState.value.baseUrl
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        resetError = "Forgot Password API not yet available on $baseUrl. Please contact support or reset your password via the backend service directly."
                    )
                }
            }
        }
    }

    fun submitResetOtp() {
        val otp = _uiState.value.resetOtpCode
        if (otp.length < 4) {
            _uiState.update { it.copy(resetError = "Please enter the 4-digit code.") }
            return
        }
        val isDummyAllowed = _uiState.value.activeEnvironment.isDummyDataAllowed
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, resetError = null) }
            delay(500)
            if (isDummyAllowed) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        resetStep = 3,
                        messageSnackbar = "Identity verified! Set your new password."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        resetError = "OTP verification API not connected to backend. OTP verification only available in TEST environment with dummy data."
                    )
                }
            }
        }
    }

    fun submitNewPassword() {
        val state = _uiState.value
        if (state.resetNewPassword.length < 6) {
            _uiState.update { it.copy(resetError = "Password must be at least 6 characters.") }
            return
        }
        if (state.resetNewPassword != state.resetConfirmPassword) {
            _uiState.update { it.copy(resetError = "Passwords do not match.") }
            return
        }

        val isDummyAllowed = _uiState.value.activeEnvironment.isDummyDataAllowed
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, resetError = null) }
            delay(600)
            if (isDummyAllowed) {
                repository.resetPassword(state.resetEmail, state.resetNewPassword)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentMode = AuthScreenMode.LOGIN,
                        loginEmail = state.resetEmail,
                        messageSnackbar = "Password reset successfully! Please sign in with your new password."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        resetError = "Password reset update API not yet available on ${state.baseUrl}. Password updates only work in TEST environment."
                    )
                }
            }
        }
    }

    fun triggerBiometricAuth() {
        val tenant = _uiState.value.selectedTenant
        val isDummyAllowed = _uiState.value.activeEnvironment.isDummyDataAllowed
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBiometricModalOpen = true,
                    isAuthRedirecting = true,
                    authRedirectMessage = "Scanning biometrics & redirecting to ${tenant.uppercase()}..."
                )
            }
            delay(1200)

            if (isDummyAllowed) {
                val user = repository.getUserByEmail("alice@jobtraq.in")
                if (user != null) {
                    sessionManager.saveLoginSession(
                        token = null,
                        email = user.email,
                        userId = user.id,
                        rememberMe = true,
                        tenantId = tenant
                    )
                    _uiState.update {
                        it.copy(
                            isBiometricModalOpen = false,
                            isAuthRedirecting = false,
                            loggedInUser = user,
                            currentMode = AuthScreenMode.LOGGED_IN,
                            messageSnackbar = "Biometric unlock successful! (${tenant.uppercase()} Tenant)"
                        )
                    }
                    startSessionHeartbeat()
                } else {
                    _uiState.update {
                        it.copy(
                            isBiometricModalOpen = false,
                            isAuthRedirecting = false,
                            loginError = "Biometric setup required."
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isBiometricModalOpen = false,
                        isAuthRedirecting = false,
                        loginError = "Biometric auth not connected to backend API on ${_uiState.value.baseUrl}. Please use email/password login in DEV/PROD environments."
                    )
                }
            }
        }
    }

    fun triggerSocialAuth(providerName: String) {
        val tenant = _uiState.value.selectedTenant
        val isDummyAllowed = _uiState.value.activeEnvironment.isDummyDataAllowed
        val baseUrl = _uiState.value.baseUrl
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isAuthRedirecting = true,
                    authRedirectMessage = "Authenticating with $providerName & redirecting to ${tenant.uppercase()}..."
                )
            }
            delay(1000)

            val mockToken = "mock-google-token-${providerName.lowercase()}user@google.com|${providerName} User|https://lh3.googleusercontent.com/a/default-user"
            val result = repository.authenticateWithGoogle(
                idToken = mockToken,
                action = "login",
                baseUrl = baseUrl,
                tenantId = tenant,
                isDummyDataAllowed = isDummyAllowed
            )

            result.onSuccess { userId ->
                val user = repository.getUserById(userId)
                if (user != null) {
                    sessionManager.saveLoginSession(
                        token = "mock-google-token-${user.id}",
                        email = user.email,
                        userId = user.id,
                        rememberMe = true,
                        tenantId = tenant
                    )
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthRedirecting = false,
                        loggedInUser = user,
                        currentMode = AuthScreenMode.LOGGED_IN,
                        messageSnackbar = "Signed in with $providerName successfully! (${tenant.uppercase()} Tenant)"
                    )
                }
                startSessionHeartbeat()
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthRedirecting = false,
                        loginError = exception.message ?: "Google OAuth failed."
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            heartbeatJob?.cancel()
            try {
                val api = RetrofitClient.createApiService(_uiState.value.baseUrl, sessionManager)
                runCatching {
                    val session = sessionManager.currentSession()
                    // Server-side logout: we could call a dedicated /api/auth/logout endpoint
                    // For now, we just clear client-side session which prevents further authenticated calls
                }
            } catch (_: Exception) {
            }
            sessionManager.clearSession()
            _uiState.update {
                it.copy(
                    loggedInUser = null,
                    authToken = null,
                    currentMode = AuthScreenMode.LOGIN,
                    postLoginProgress = null,
                    postLoginInterviews = null,
                    messageSnackbar = "Logged out safely."
                )
            }
        }
    }

    private fun forceLogoutInternal() {
        viewModelScope.launch {
            heartbeatJob?.cancel()
            sessionManager.clearSession()
            _uiState.update {
                it.copy(
                    loggedInUser = null,
                    authToken = null,
                    currentMode = AuthScreenMode.LOGIN,
                    postLoginProgress = null,
                    postLoginInterviews = null
                )
            }
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(messageSnackbar = null) }
    }

    fun setTermsModalOpen(open: Boolean) {
        _uiState.update { it.copy(isTermsModalOpen = open) }
    }

    fun setEditProfileModalOpen(open: Boolean) {
        _uiState.update { it.copy(isEditProfileModalOpen = open) }
    }

    fun toggleThemeOverride() {
        _uiState.update { it.copy(darkThemeOverride = !it.darkThemeOverride) }
    }

    fun updateProfile(name: String, phone: String, avatarIndex: Int) {
        val currentUser = _uiState.value.loggedInUser ?: return
        viewModelScope.launch {
            val updated = currentUser.copy(
                fullName = name,
                phone = phone,
                avatarBadgeIndex = avatarIndex
            )
            repository.updateUserProfile(updated)
            _uiState.update {
                it.copy(
                    loggedInUser = updated,
                    isEditProfileModalOpen = false,
                    messageSnackbar = "Profile updated!"
                )
            }
        }
    }

    private fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength()

        val minLen = password.length >= 8
        val hasUpper = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }

        var score = 0
        if (password.length >= 6) score++
        if (minLen) score++
        if (hasUpper && hasDigit) score++
        if (hasSpecial) score++

        return PasswordStrength(
            score = score.coerceIn(0, 4),
            hasMinLength = minLen,
            hasUppercase = hasUpper,
            hasDigit = hasDigit,
            hasSpecialChar = hasSpecial
        )
    }
}
