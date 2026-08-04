package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private fun createMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    fun createApiService(baseUrl: String, sessionManager: SessionManager? = null): JobTraqMobileApiService {
        val withScheme = when {
            baseUrl.startsWith("http://", ignoreCase = true) ||
                baseUrl.startsWith("https://", ignoreCase = true) -> baseUrl
            else -> "https://$baseUrl"
        }
        val cleanUrl = if (withScheme.endsWith("/")) withScheme else "$withScheme/"
        val okHttpBuilder = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
        if (sessionManager != null) {
            okHttpBuilder.addInterceptor(AuthInterceptor(sessionManager))
        }
        val okHttpClient = okHttpBuilder.build()

        val retrofit = Retrofit.Builder()
            .baseUrl(cleanUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(createMoshi()))
            .build()

        return retrofit.create(JobTraqMobileApiService::class.java)
    }
}

object ApiErrorSanitizer {

    private fun looksLikeHtml(raw: String): Boolean {
        if (raw.isBlank()) return false
        val up = raw.trim().uppercase()
        return up.startsWith("<!DOCTYPE") ||
            up.startsWith("<HTML") ||
            up.contains("<HEAD") ||
            up.contains("<BODY") ||
            up.contains("<TITLE") ||
            up.contains("<NEXT/") ||
            up.contains("_NEXT/STATIC/") ||
            up.contains("<SCRIPT") ||
            up.contains("<LINK REL=") ||
            up.contains("<META CHAR")
    }

    fun sanitizeApiError(
        rawError: String?,
        responseCode: Int,
        baseUrl: String,
        fallbackContext: String
    ): String {
        val trimmed = (rawError ?: "").trim()
        val first200 = if (trimmed.length > 200) trimmed.substring(0, 200) else trimmed
        val htmlDetected = looksLikeHtml(trimmed)
        val looksLikeWeb404 = responseCode in 400..499 && (htmlDetected || first200.contains("route does not match", ignoreCase = true) || first200.contains("not found", ignoreCase = true))

        return when {
            looksLikeWeb404 -> {
                "No API endpoint at $baseUrl (HTTP $responseCode). $baseUrl returned a website/HTML page instead of JSON — it looks like a frontend web host, not an API server. In Settings, pick a real API URL (JobTraq PROD API, or your local backend with http://10.0.2.2:PORT for emulator)."
            }
            htmlDetected -> {
                "Unexpected HTML response from $baseUrl (HTTP $responseCode). This URL points to a web page (not a JSON API). Please verify the Base URL in Settings."
            }
            trimmed.isBlank() -> {
                "API Error (HTTP $responseCode): Failed to $fallbackContext on $baseUrl — server returned empty error details."
            }
            trimmed.length > 400 -> {
                "API Error (HTTP $responseCode): ${first200}… — check your server logs for the complete response body."
            }
            else -> {
                "API Error (HTTP $responseCode): $trimmed"
            }
        }
    }

    fun sanitizeExceptionError(e: Exception, baseUrl: String): String {
        val msg = (e.localizedMessage ?: e.message ?: "").trim()
        val causeChain = generateSequence(e.cause) { it.cause }
            .mapNotNull { it.localizedMessage ?: it.message }
            .joinToString(" → ")
        val combined = (msg + " " + causeChain).lowercase()
        val first220Msg = if (msg.length > 220) msg.substring(0, 220) + "…" else msg

        return when {
            combined.contains("failed to connect") ||
                combined.contains("connection refused") ||
                combined.contains("etimedout") ||
                combined.contains("econnrefused") ||
                combined.contains("unknownhost") ||
                combined.contains("host not found") ||
                combined.contains("unreachable") -> {
                val hint = if (baseUrl.contains("localhost", ignoreCase = true) || baseUrl.contains("127.0.0.1")) {
                    " On Android, use http://10.0.2.2:<PORT> instead of localhost for the emulator to reach your PC, or use your PC LAN IP for a physical device."
                } else ""
                "Could not reach $baseUrl — server is down, unreachable, or URL is wrong.$hint"
            }
            combined.contains("cleartext") || combined.contains("cletext") || combined.contains("http traffic") -> {
                "Cleartext HTTP blocked for $baseUrl. Use https://, or keep android:usesCleartextTraffic=\"true\" (already enabled in this app)."
            }
            combined.contains("ssl") || combined.contains("certpath") || combined.contains("handshake") || combined.contains("certificate") -> {
                "TLS/SSL certificate error connecting to $baseUrl — verify HTTPS cert is valid, or add your custom CA if using self-signed certs."
            }
            combined.contains("timeout") -> {
                "Request timed out to $baseUrl — server is slow or network is unstable."
            }
            msg.isBlank() -> {
                "Unknown network error while calling $baseUrl."
            }
            else -> {
                "API connection failed to $baseUrl: $first220Msg"
            }
        }
    }

    fun safeRawError(body: okhttp3.ResponseBody?): String? = runCatching { body?.string() }.getOrNull()
}

data class LoginResult(
    val user: UserEntity,
    val token: String? = null,
    val dnsUnreachableFallback: Boolean = false
)

class AuthRepository(private val userDao: UserDao) {

    // Simple SHA-256 password hashing helper
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    suspend fun registerUser(
        fullName: String,
        email: String,
        passwordRaw: String,
        phone: String = "",
        avatarBadgeIndex: Int = 0,
        baseUrl: String = "http://localhost:9002",
        tenantId: String = "platform",
        isDummyDataAllowed: Boolean = false
    ): Result<Long> {
        val trimmedEmail = email.trim().lowercase()

        val urlsToTry = mutableListOf(baseUrl)
        val alt = BaseUrlResolver.alternateJobtraqHostForDnsFallback(baseUrl)
        if (alt != null) urlsToTry.add(alt)
        var lastNetworkException: Exception? = null
        var allFailedDueToDns: Boolean = urlsToTry.size > 0
        var dnsRetryCount = 0

        for (candidateUrl in urlsToTry) {
            if (candidateUrl.isNotBlank()) {
                try {
                    val apiService = RetrofitClient.createApiService(candidateUrl)
                    val response = apiService.signup(
                        ApiSignupRequest(
                            name = fullName.trim(),
                            email = trimmedEmail,
                            password = passwordRaw,
                            tenantId = tenantId
                        )
                    )
                    allFailedDueToDns = false
                    if (response.isSuccessful && response.body() != null) {
                        val existing = userDao.getUserByEmail(trimmedEmail)
                        if (existing != null) {
                            val updated = existing.copy(
                                fullName = fullName.trim(),
                                passwordHash = hashPassword(passwordRaw),
                                phone = phone.trim().ifBlank { existing.phone },
                                avatarBadgeIndex = avatarBadgeIndex
                            )
                            userDao.updateUser(updated)
                            return Result.success(existing.id)
                        }
                        val newUser = UserEntity(
                            fullName = fullName.trim().ifBlank { "Platform Member" },
                            email = trimmedEmail,
                            passwordHash = hashPassword(passwordRaw),
                            phone = phone.trim(),
                            avatarBadgeIndex = avatarBadgeIndex
                        )
                        val id = userDao.insertUser(newUser)
                        return Result.success(id)
                    } else {
                        val rawError = ApiErrorSanitizer.safeRawError(response.errorBody())
                        val errorMsg = ApiErrorSanitizer.sanitizeApiError(
                            rawError = rawError,
                            responseCode = response.code(),
                            baseUrl = candidateUrl,
                            fallbackContext = "create account/signup"
                        )
                        if (!isDummyDataAllowed) {
                            return Result.failure(Exception(errorMsg))
                        }
                    }
                } catch (e: Exception) {
                    lastNetworkException = e
                    val dns = BaseUrlResolver.isDnsFailure(e)
                    if (!dns) {
                        allFailedDueToDns = false
                        if (!isDummyDataAllowed) {
                            return Result.failure(Exception(ApiErrorSanitizer.sanitizeExceptionError(e, candidateUrl)))
                        }
                    } else {
                        dnsRetryCount++
                    }
                }
            }
        }

        val totalDnsAttempts = urlsToTry.count { url ->
            val host = url.trim().lowercase().removePrefix("https://").removePrefix("http://").substringBefore("/")
            host == "www.jobtraq.in" || host == "jobtraq.in" || host.endsWith(".jobtraq.in")
        }
        val jobtraqDnsAllFailed: Boolean = (totalDnsAttempts > 0) && allFailedDueToDns ||
            (allFailedDueToDns && dnsRetryCount >= totalDnsAttempts.coerceAtLeast(1))

        if (lastNetworkException != null && !jobtraqDnsAllFailed && !isDummyDataAllowed) {
            return Result.failure(Exception(ApiErrorSanitizer.sanitizeExceptionError(lastNetworkException, baseUrl)))
        }

        // 2. Fallback to Local Room Database registration
        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            val updated = existing.copy(
                fullName = fullName.trim().ifBlank { existing.fullName },
                passwordHash = hashPassword(passwordRaw),
                phone = phone.trim().ifBlank { existing.phone },
                avatarBadgeIndex = avatarBadgeIndex
            )
            return try {
                userDao.updateUser(updated)
                Result.success(existing.id)
            } catch (e: Exception) {
                Result.success(existing.id)
            }
        }

        val newUser = UserEntity(
            fullName = fullName.trim().ifBlank { "Platform Member" },
            email = trimmedEmail,
            passwordHash = hashPassword(passwordRaw),
            phone = phone.trim(),
            avatarBadgeIndex = avatarBadgeIndex
        )

        return try {
            val id = userDao.insertUser(newUser)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to register account: ${e.localizedMessage}"))
        }
    }

    suspend fun loginUser(
        email: String,
        passwordRaw: String,
        baseUrl: String = "http://localhost:9002",
        tenantId: String = "platform",
        isDummyDataAllowed: Boolean = false
    ): Result<LoginResult> {
        val trimmedEmail = email.trim().lowercase()

        val urlsToTry = mutableListOf(baseUrl)
        val alt = BaseUrlResolver.alternateJobtraqHostForDnsFallback(baseUrl)
        if (alt != null) urlsToTry.add(alt)
        var lastNetworkException: Exception? = null
        var allFailedDueToDns: Boolean = urlsToTry.size > 0
        var dnsRetryCount = 0

        for (candidateUrl in urlsToTry) {
            if (candidateUrl.isNotBlank()) {
                try {
                    val apiService = RetrofitClient.createApiService(candidateUrl)
                    val response = apiService.login(ApiLoginRequest(trimmedEmail, passwordRaw, tenantId))
                    allFailedDueToDns = false
                    if (response.isSuccessful && response.body() != null) {
                        val authBody = response.body()!!
                        val apiUser = authBody.user
                        val name = apiUser?.name?.ifBlank { null }
                            ?: trimmedEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
                        val token = authBody.token

                        var localUser = userDao.getUserByEmail(trimmedEmail)
                        if (localUser == null) {
                            val newUser = UserEntity(
                                fullName = name,
                                email = trimmedEmail,
                                passwordHash = hashPassword(passwordRaw),
                                phone = "+1 (555) 019-2834",
                                avatarBadgeIndex = 1
                            )
                            userDao.insertUser(newUser)
                            localUser = userDao.getUserByEmail(trimmedEmail)
                        } else {
                            val updated = localUser.copy(fullName = name, passwordHash = hashPassword(passwordRaw))
                            userDao.updateUser(updated)
                            localUser = updated
                        }
                        val resolvedUser = localUser ?: UserEntity(fullName = name, email = trimmedEmail, passwordHash = "")
                        return Result.success(LoginResult(user = resolvedUser, token = token))
                    } else {
                        val rawError = ApiErrorSanitizer.safeRawError(response.errorBody())
                        val errorMsg = ApiErrorSanitizer.sanitizeApiError(
                            rawError = rawError,
                            responseCode = response.code(),
                            baseUrl = candidateUrl,
                            fallbackContext = "login (invalid credentials or missing endpoint)"
                        )
                        if (!isDummyDataAllowed || response.code() in 400..499) {
                            return Result.failure(Exception(errorMsg))
                        }
                    }
                } catch (e: Exception) {
                    lastNetworkException = e
                    val dns = BaseUrlResolver.isDnsFailure(e)
                    if (!dns) {
                        allFailedDueToDns = false
                        if (!isDummyDataAllowed) {
                            return Result.failure(Exception(ApiErrorSanitizer.sanitizeExceptionError(e, candidateUrl)))
                        }
                    } else {
                        dnsRetryCount++
                    }
                }
            }
        }

        val totalDnsAttempts = urlsToTry.count { url ->
            val host = url.trim().lowercase().removePrefix("https://").removePrefix("http://").substringBefore("/")
            host == "www.jobtraq.in" || host == "jobtraq.in" || host.endsWith(".jobtraq.in")
        }
        val jobtraqDnsAllFailed: Boolean = (totalDnsAttempts > 0) && (dnsRetryCount >= totalDnsAttempts.coerceAtLeast(urlsToTry.size)) ||
            (allFailedDueToDns && dnsRetryCount >= totalDnsAttempts) ||
            (allFailedDueToDns && urlsToTry.size > 0)

        if (lastNetworkException != null && !jobtraqDnsAllFailed && !isDummyDataAllowed) {
            return Result.failure(Exception(ApiErrorSanitizer.sanitizeExceptionError(lastNetworkException, baseUrl)))
        }

        var user = userDao.getUserByEmail(trimmedEmail)
        if (user == null) {
            val namePart = trimmedEmail.substringBefore("@").replace(".", " ").replace("_", " ")
            val formattedName = namePart.split(" ")
                .filter { it.isNotBlank() }
                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            val newUser = UserEntity(
                fullName = if (formattedName.isNotBlank()) formattedName else "Platform User",
                email = trimmedEmail,
                passwordHash = hashPassword(passwordRaw),
                phone = "+1 (555) 019-2834",
                avatarBadgeIndex = 1
            )
            userDao.insertUser(newUser)
            user = userDao.getUserByEmail(trimmedEmail)
        }

        if (user != null) {
            val warnToken = if (jobtraqDnsAllFailed) null else null
            return Result.success(LoginResult(user = user, token = warnToken, dnsUnreachableFallback = jobtraqDnsAllFailed))
        } else {
            return Result.failure(Exception("Authentication failed. Please try again."))
        }
    }

    suspend fun authenticateWithGoogle(
        idToken: String,
        action: String,
        baseUrl: String = "http://localhost:9002",
        tenantId: String = "platform",
        referralCode: String? = null,
        partnerCode: String? = null,
        isDummyDataAllowed: Boolean = false
    ): Result<Long> {
        val urlsToTry = mutableListOf(baseUrl)
        val alt = BaseUrlResolver.alternateJobtraqHostForDnsFallback(baseUrl)
        if (alt != null) urlsToTry.add(alt)
        var lastNetworkException: Exception? = null
        var allFailedDueToDns: Boolean = urlsToTry.size > 0
        var dnsRetryCount = 0

        for (candidateUrl in urlsToTry) {
            if (candidateUrl.isNotBlank()) {
                try {
                    val apiService = RetrofitClient.createApiService(candidateUrl)
                    val response = apiService.googleAuth(
                        ApiGoogleAuthRequest(
                            idToken = idToken,
                            action = action,
                            tenantId = tenantId,
                            referralCode = referralCode,
                            partnerCode = partnerCode
                        )
                    )
                    allFailedDueToDns = false
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        val apiUser = body.user
                        if (apiUser != null) {
                            val existing = userDao.getUserByEmail(apiUser.email)
                            if (existing != null) {
                                val updated = existing.copy(
                                    fullName = apiUser.name
                                )
                                userDao.updateUser(updated)
                                return Result.success(existing.id)
                            }
                            val newUser = UserEntity(
                                fullName = apiUser.name,
                                email = apiUser.email,
                                passwordHash = "",
                                phone = "",
                                avatarBadgeIndex = 2
                            )
                            val id = userDao.insertUser(newUser)
                            return Result.success(id)
                        }
                    } else {
                        val rawError = ApiErrorSanitizer.safeRawError(response.errorBody())
                        val errorMsg = ApiErrorSanitizer.sanitizeApiError(
                            rawError = rawError,
                            responseCode = response.code(),
                            baseUrl = candidateUrl,
                            fallbackContext = "Google authentication ($action)"
                        )
                        if (!isDummyDataAllowed) {
                            return Result.failure(Exception(errorMsg))
                        }
                    }
                } catch (e: Exception) {
                    lastNetworkException = e
                    val dns = BaseUrlResolver.isDnsFailure(e)
                    if (!dns) {
                        allFailedDueToDns = false
                        if (!isDummyDataAllowed) {
                            return Result.failure(Exception(ApiErrorSanitizer.sanitizeExceptionError(e, candidateUrl)))
                        }
                    } else {
                        dnsRetryCount++
                    }
                }
            }
        }

        val demoEmail = if (idToken.startsWith("mock-google-token-")) {
            idToken.split("|").getOrNull(1) ?: "mockuser@google.com"
        } else {
            "googleuser@auth.io"
        }
        val demoName = if (idToken.startsWith("mock-google-token-")) {
            idToken.split("|").getOrNull(2) ?: "Google User"
        } else {
            "Google User"
        }

        val existing = userDao.getUserByEmail(demoEmail)
        if (existing != null) {
            return Result.success(existing.id)
        }
        val newUser = UserEntity(
            fullName = demoName,
            email = demoEmail,
            passwordHash = "",
            phone = "",
            avatarBadgeIndex = 2
        )
        val id = userDao.insertUser(newUser)
        return Result.success(id)
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email.trim().lowercase())
    }

    suspend fun getUserById(userId: Long): UserEntity? {
        return userDao.getUserById(userId)
    }

    fun getUserFlow(userId: Long): Flow<UserEntity?> {
        return userDao.getUserByIdFlow(userId)
    }

    suspend fun resetPassword(email: String, newPasswordRaw: String): Result<Unit> {
        val trimmedEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(trimmedEmail)
            ?: return Result.failure(Exception("No user found with email $email"))

        userDao.updatePassword(trimmedEmail, hashPassword(newPasswordRaw))
        return Result.success(Unit)
    }

    suspend fun updateUserProfile(user: UserEntity): Result<Unit> {
        return try {
            userDao.updateUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun seedDemoUserIfEmpty() {
        if (userDao.getUserCount() == 0) {
            registerUser(
                fullName = "Alex Rivera",
                email = "alice@jobtraq.in",
                passwordRaw = "password123",
                phone = "+1 (555) 019-2834",
                avatarBadgeIndex = 0
            )
        }
    }
}
