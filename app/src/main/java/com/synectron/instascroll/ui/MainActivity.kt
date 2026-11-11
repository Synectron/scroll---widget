package com.synectron.instascroll.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.synectron.instascroll.R
import com.synectron.instascroll.data.ScrollRepository
import com.synectron.instascroll.service.OverlayService
import com.synectron.instascroll.service.ScrollAccessibilityService
import com.synectron.instascroll.widget.ScrollAppWidgetProvider

class MainActivity : AppCompatActivity() {

    private lateinit var repository: ScrollRepository
    private lateinit var todayCount: TextView
    private lateinit var todayDistance: TextView
    private lateinit var totalCount: TextView
    private lateinit var weekList: TextView
    private lateinit var accessibilityStatus: TextView
    private lateinit var overlayStatus: TextView
    private lateinit var overlaySwitch: MaterialSwitch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = ScrollRepository(this)
        todayCount = findViewById(R.id.today_count)
        todayDistance = findViewById(R.id.today_distance)
        totalCount = findViewById(R.id.total_count)
        weekList = findViewById(R.id.week_list)
        accessibilityStatus = findViewById(R.id.accessibility_status)
        overlayStatus = findViewById(R.id.overlay_status)
        overlaySwitch = findViewById(R.id.overlay_switch)

        findViewById<MaterialButton>(R.id.btn_enable_accessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        findViewById<MaterialButton>(R.id.btn_enable_overlay).setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
        findViewById<MaterialButton>(R.id.btn_reset_today).setOnClickListener {
            repository.resetToday()
            refreshUi()
            ScrollAppWidgetProvider.requestUpdate(this)
            if (OverlayService.canDrawOverlays(this) && repository.isOverlayEnabled()) {
                OverlayService.updateCount(this, 0, 0L)
            }
        }

        overlaySwitch.isChecked = repository.isOverlayEnabled()
        overlaySwitch.setOnCheckedChangeListener { _, checked ->
            repository.setOverlayEnabled(checked)
            if (checked && OverlayService.canDrawOverlays(this)) {
                OverlayService.start(this)
            } else {
                OverlayService.stop(this)
            }
            refreshUi()
        }

        maybeRequestNotifications()
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
        if (isAccessibilityEnabled() &&
            OverlayService.canDrawOverlays(this) &&
            repository.isOverlayEnabled()
        ) {
            OverlayService.start(this)
        }
    }

    private fun refreshUi() {
        val snap = repository.snapshot()
        todayCount.text = snap.todayScrolls.toString()
        todayDistance.text = getString(
            R.string.distance_format,
            formatDistance(snap.todayDistancePx)
        )
        totalCount.text = getString(R.string.total_scrolls_format, snap.totalScrolls)

        val week = repository.recentDays(7).joinToString("\n") { (day, stats) ->
            "$day  ·  ${stats.scrolls} scrolls"
        }
        weekList.text = week

        val a11yOn = isAccessibilityEnabled()
        accessibilityStatus.text = getString(
            if (a11yOn) R.string.status_accessibility_on else R.string.status_accessibility_off
        )
        accessibilityStatus.setTextColor(
            ContextCompat.getColor(
                this,
                if (a11yOn) R.color.status_on else R.color.status_off
            )
        )

        val overlayOn = OverlayService.canDrawOverlays(this)
        overlayStatus.text = getString(
            if (overlayOn) R.string.status_overlay_on else R.string.status_overlay_off
        )
        overlayStatus.setTextColor(
            ContextCompat.getColor(
                this,
                if (overlayOn) R.color.status_on else R.color.status_off
            )
        )
    }

    private fun isAccessibilityEnabled(): Boolean {
        val manager = getSystemService(AccessibilityManager::class.java) ?: return false
        val enabled = manager.getEnabledAccessibilityServiceList(
            android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )
        val expected = "$packageName/${ScrollAccessibilityService::class.java.name}"
        return enabled.any { it.id == expected || it.id.contains("ScrollAccessibilityService") }
    }

    private fun maybeRequestNotifications() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) return
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            1001
        )
    }

    private fun formatDistance(px: Long): String {
        return when {
            px < 1_000 -> "$px px"
            px < 100_000 -> String.format("%.1fk px", px / 1000.0)
            else -> "${px / 1000}k px"
        }
    }
}
