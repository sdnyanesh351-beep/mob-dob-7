package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private val Context.streakDataStore: DataStore<Preferences> by preferencesDataStore(name = "jobtraq_daily_streak")

data class StreakData(
    val streakDays: Int = 1,
    val lastLoginDate: String = "",
    val bestStreak: Int = 1,
    val totalLogins: Int = 1,
    val streakFreezes: Int = 1,
    val isLoginRecordedToday: Boolean = true,
    val milestoneMessage: String? = null
)

class StreakDataStoreManager(private val context: Context) {

    private object Keys {
        val STREAK_DAYS = intPreferencesKey("streak_days")
        val LAST_LOGIN_DATE = stringPreferencesKey("last_login_date")
        val BEST_STREAK = intPreferencesKey("best_streak")
        val TOTAL_LOGINS = intPreferencesKey("total_logins")
        val STREAK_FREEZES = intPreferencesKey("streak_freezes")
    }

    val streakDataFlow: Flow<StreakData> = context.streakDataStore.data.map { prefs ->
        val streak = prefs[Keys.STREAK_DAYS] ?: 1
        val lastDate = prefs[Keys.LAST_LOGIN_DATE] ?: ""
        val best = prefs[Keys.BEST_STREAK] ?: maxOf(1, streak)
        val total = prefs[Keys.TOTAL_LOGINS] ?: 1
        val freezes = prefs[Keys.STREAK_FREEZES] ?: 1

        val todayStr = getTodayDateString()
        val isToday = lastDate == todayStr

        StreakData(
            streakDays = streak,
            lastLoginDate = lastDate,
            bestStreak = maxOf(best, streak),
            totalLogins = total,
            streakFreezes = freezes,
            isLoginRecordedToday = isToday,
            milestoneMessage = getMilestoneMessage(streak)
        )
    }

    suspend fun getStreakData(): StreakData = streakDataFlow.first()

    suspend fun recordDailyLogin(): StreakData {
        val todayDateStr = getTodayDateString()
        var updatedStreakData = StreakData()

        context.streakDataStore.edit { prefs ->
            val lastDateStr = prefs[Keys.LAST_LOGIN_DATE] ?: ""
            val currentStreak = prefs[Keys.STREAK_DAYS] ?: 0
            val bestStreak = prefs[Keys.BEST_STREAK] ?: currentStreak
            val totalLogins = prefs[Keys.TOTAL_LOGINS] ?: 0
            var streakFreezes = prefs[Keys.STREAK_FREEZES] ?: 1

            val newStreak: Int
            if (lastDateStr.isEmpty()) {
                // First login ever
                newStreak = 1
            } else if (lastDateStr == todayDateStr) {
                // Already recorded today
                newStreak = if (currentStreak <= 0) 1 else currentStreak
            } else {
                val daysBetween = calculateDaysBetween(lastDateStr, todayDateStr)
                when {
                    daysBetween == 1 -> {
                        // Consecutive day login!
                        newStreak = currentStreak + 1
                    }
                    daysBetween > 1 -> {
                        // Missed days
                        if (streakFreezes > 0) {
                            streakFreezes -= 1
                            newStreak = if (currentStreak <= 0) 1 else currentStreak
                        } else {
                            newStreak = 1
                        }
                    }
                    else -> {
                        newStreak = if (currentStreak <= 0) 1 else currentStreak
                    }
                }
            }

            val newBest = maxOf(bestStreak, newStreak)
            val newTotal = totalLogins + 1

            prefs[Keys.STREAK_DAYS] = newStreak
            prefs[Keys.LAST_LOGIN_DATE] = todayDateStr
            prefs[Keys.BEST_STREAK] = newBest
            prefs[Keys.TOTAL_LOGINS] = newTotal
            prefs[Keys.STREAK_FREEZES] = streakFreezes

            updatedStreakData = StreakData(
                streakDays = newStreak,
                lastLoginDate = todayDateStr,
                bestStreak = newBest,
                totalLogins = newTotal,
                streakFreezes = streakFreezes,
                isLoginRecordedToday = true,
                milestoneMessage = getMilestoneMessage(newStreak)
            )
        }

        return updatedStreakData
    }

    suspend fun addStreakFreeze(count: Int = 1) {
        context.streakDataStore.edit { prefs ->
            val current = prefs[Keys.STREAK_FREEZES] ?: 1
            prefs[Keys.STREAK_FREEZES] = current + count
        }
    }

    suspend fun overrideStreakData(currentStreak: Int, longestStreak: Int, freezes: Int, isActiveToday: Boolean = true) {
        val todayStr = getTodayDateString()
        context.streakDataStore.edit { prefs ->
            prefs[Keys.STREAK_DAYS] = maxOf(0, currentStreak)
            prefs[Keys.BEST_STREAK] = maxOf(1, longestStreak, currentStreak)
            prefs[Keys.STREAK_FREEZES] = maxOf(0, freezes)
            if (isActiveToday) {
                prefs[Keys.LAST_LOGIN_DATE] = todayStr
                val total = (prefs[Keys.TOTAL_LOGINS] ?: 1)
                val lastDate = prefs[Keys.LAST_LOGIN_DATE]
                if (lastDate != todayStr) {
                    prefs[Keys.TOTAL_LOGINS] = total + 1
                }
            }
        }
    }

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    private fun calculateDaysBetween(startDateStr: String, endDateStr: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val startDate = sdf.parse(startDateStr) ?: return 2
            val endDate = sdf.parse(endDateStr) ?: return 2
            val diffMs = endDate.time - startDate.time
            val days = TimeUnit.MILLISECONDS.toDays(diffMs).toInt()
            if (days < 0) 0 else days
        } catch (e: Exception) {
            2
        }
    }

    private fun getMilestoneMessage(streak: Int): String? {
        return when (streak) {
            1 -> "🚀 Day 1: Great start to your job search!"
            3 -> "🔥 3-Day Streak! Consistent effort pays off."
            5 -> "⚡ 5-Day Streak! Halfway to a full week."
            7 -> "🏆 7-Day Streak! 1 full week of daily progress."
            14 -> "⭐ 14-Day Streak! 2 weeks of amazing dedication."
            30 -> "👑 30-Day Master Seeker! 1 Month Strong!"
            else -> if (streak % 5 == 0) "🔥 $streak-Day Streak active!" else null
        }
    }
}
