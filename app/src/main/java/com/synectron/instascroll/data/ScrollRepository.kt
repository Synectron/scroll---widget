package com.synectron.instascroll.data

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DayStats(
    val scrolls: Int = 0,
    val distancePx: Long = 0L
)

data class ScrollSnapshot(
    val todayKey: String,
    val todayScrolls: Int,
    val todayDistancePx: Long,
    val totalScrolls: Int,
    val totalDistancePx: Long
)

class ScrollRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun snapshot(): ScrollSnapshot {
        val today = todayKey()
        val todayScrolls = prefs.getInt(dayScrollsKey(today), 0)
        val todayDistance = prefs.getLong(dayDistanceKey(today), 0L)
        return ScrollSnapshot(
            todayKey = today,
            todayScrolls = todayScrolls,
            todayDistancePx = todayDistance,
            totalScrolls = prefs.getInt(KEY_TOTAL_SCROLLS, 0),
            totalDistancePx = prefs.getLong(KEY_TOTAL_DISTANCE, 0L)
        )
    }

    fun recordScroll(distancePx: Long = 0L, scrolls: Int = 1): ScrollSnapshot {
        val today = todayKey()
        val nextTodayScrolls = prefs.getInt(dayScrollsKey(today), 0) + scrolls
        val nextTodayDistance = prefs.getLong(dayDistanceKey(today), 0L) + distancePx
        val nextTotalScrolls = prefs.getInt(KEY_TOTAL_SCROLLS, 0) + scrolls
        val nextTotalDistance = prefs.getLong(KEY_TOTAL_DISTANCE, 0L) + distancePx

        prefs.edit()
            .putInt(dayScrollsKey(today), nextTodayScrolls)
            .putLong(dayDistanceKey(today), nextTodayDistance)
            .putInt(KEY_TOTAL_SCROLLS, nextTotalScrolls)
            .putLong(KEY_TOTAL_DISTANCE, nextTotalDistance)
            .apply()

        return ScrollSnapshot(
            todayKey = today,
            todayScrolls = nextTodayScrolls,
            todayDistancePx = nextTodayDistance,
            totalScrolls = nextTotalScrolls,
            totalDistancePx = nextTotalDistance
        )
    }

    fun resetToday(): ScrollSnapshot {
        val current = snapshot()
        val today = current.todayKey
        prefs.edit()
            .putInt(dayScrollsKey(today), 0)
            .putLong(dayDistanceKey(today), 0L)
            .putInt(KEY_TOTAL_SCROLLS, (current.totalScrolls - current.todayScrolls).coerceAtLeast(0))
            .putLong(
                KEY_TOTAL_DISTANCE,
                (current.totalDistancePx - current.todayDistancePx).coerceAtLeast(0L)
            )
            .apply()
        return snapshot()
    }

    fun isOverlayEnabled(): Boolean = prefs.getBoolean(KEY_OVERLAY_ENABLED, true)

    fun setOverlayEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_OVERLAY_ENABLED, enabled).apply()
    }

    fun recentDays(limit: Int = 7): List<Pair<String, DayStats>> {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val today = LocalDate.now()
        return (0 until limit).map { offset ->
            val day = today.minusDays(offset.toLong()).format(formatter)
            day to DayStats(
                scrolls = prefs.getInt(dayScrollsKey(day), 0),
                distancePx = prefs.getLong(dayDistanceKey(day), 0L)
            )
        }
    }

    companion object {
        private const val PREFS = "insta_scroll_stats"
        private const val KEY_TOTAL_SCROLLS = "total_scrolls"
        private const val KEY_TOTAL_DISTANCE = "total_distance"
        private const val KEY_OVERLAY_ENABLED = "overlay_enabled"

        fun todayKey(): String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        private fun dayScrollsKey(day: String) = "day_${day}_scrolls"
        private fun dayDistanceKey(day: String) = "day_${day}_distance"
    }
}
