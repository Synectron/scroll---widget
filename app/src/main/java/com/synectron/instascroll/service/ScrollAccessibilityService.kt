package com.synectron.instascroll.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.synectron.instascroll.data.ScrollRepository
import com.synectron.instascroll.widget.ScrollAppWidgetProvider
import kotlin.math.abs

class ScrollAccessibilityService : AccessibilityService() {

    private lateinit var repository: ScrollRepository

    private var lastEventAt = 0L
    private var pendingDistance = 0L

    override fun onServiceConnected() {
        repository = ScrollRepository(this)
        serviceInfo = serviceInfo?.apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_SCROLLED or
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 40
            packageNames = arrayOf(INSTAGRAM_PACKAGE)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.packageName?.toString() != INSTAGRAM_PACKAGE) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> handleScroll(event)
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> ensureOverlay()
        }
    }

    private fun handleScroll(event: AccessibilityEvent) {
        val now = System.currentTimeMillis()
        val scrollY = abs(event.scrollY)
        val fromIndex = abs(event.fromIndex)
        val toIndex = abs(event.toIndex)
        val itemDelta = abs(toIndex - fromIndex)
        val distance = when {
            scrollY > 0 -> scrollY.toLong()
            itemDelta > 0 -> itemDelta * APPROX_ITEM_HEIGHT_PX
            else -> DEFAULT_SCROLL_DISTANCE_PX
        }

        pendingDistance += distance

        val shouldCountGesture = now - lastEventAt >= GESTURE_GAP_MS
        val unitsFromDistance = (pendingDistance / SCROLL_UNIT_PX).toInt()

        if (!shouldCountGesture && unitsFromDistance <= 0) return

        val scrolls = when {
            shouldCountGesture && unitsFromDistance <= 0 -> 1
            unitsFromDistance > 0 -> unitsFromDistance.coerceAtLeast(if (shouldCountGesture) 1 else 0)
            else -> 0
        }
        if (scrolls <= 0) return

        val consumed = scrolls * SCROLL_UNIT_PX
        pendingDistance = (pendingDistance - consumed).coerceAtLeast(0L)
        lastEventAt = now

        val snapshot = repository.recordScroll(distancePx = consumed, scrolls = scrolls)
        OverlayService.updateCount(this, snapshot.todayScrolls, snapshot.todayDistancePx)
        ScrollAppWidgetProvider.requestUpdate(this)
    }

    private fun ensureOverlay() {
        if (!repository.isOverlayEnabled()) return
        if (!OverlayService.canDrawOverlays(this)) return
        startForegroundService(Intent(this, OverlayService::class.java))
    }

    override fun onInterrupt() = Unit

    companion object {
        const val INSTAGRAM_PACKAGE = "com.instagram.android"
        private const val GESTURE_GAP_MS = 220L
        private const val SCROLL_UNIT_PX = 280L
        private const val DEFAULT_SCROLL_DISTANCE_PX = 160L
        private const val APPROX_ITEM_HEIGHT_PX = 320L
    }
}
