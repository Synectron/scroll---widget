package com.synectron.instascroll.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.synectron.instascroll.R
import com.synectron.instascroll.data.ScrollRepository
import com.synectron.instascroll.ui.MainActivity
import kotlin.math.roundToInt

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var repository: ScrollRepository
    private var overlayView: View? = null
    private var countView: TextView? = null
    private var subView: TextView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var minimized = false

    override fun onCreate() {
        super.onCreate()
        repository = ScrollRepository(this)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startAsForeground()
        if (canDrawOverlays(this) && repository.isOverlayEnabled()) {
            showOverlay()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_UPDATE_COUNT -> {
                val scrolls = intent.getIntExtra(EXTRA_SCROLLS, repository.snapshot().todayScrolls)
                val distance = intent.getLongExtra(EXTRA_DISTANCE, repository.snapshot().todayDistancePx)
                render(scrolls, distance)
            }
            else -> {
                val snap = repository.snapshot()
                render(snap.todayScrolls, snap.todayDistancePx)
            }
        }
        return START_STICKY
    }

    private fun startAsForeground() {
        val channelId = "insta_scroll_overlay"
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                channelId,
                getString(R.string.notification_channel),
                NotificationManager.IMPORTANCE_LOW
            )
        )

        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_body))
            .setSmallIcon(R.drawable.ic_stat_scroll)
            .setContentIntent(openApp)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun showOverlay() {
        if (overlayView != null) return

        val view = LayoutInflater.from(this).inflate(R.layout.overlay_widget, null)
        countView = view.findViewById(R.id.overlay_count)
        subView = view.findViewById(R.id.overlay_sub)
        val toggle = view.findViewById<ImageButton>(R.id.overlay_toggle)
        val label = view.findViewById<TextView>(R.id.overlay_label)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(18)
            y = dp(160)
        }

        var dragX = 0
        var dragY = 0
        var startX = 0
        var startY = 0

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dragX = params.x
                    dragY = params.y
                    startX = event.rawX.roundToInt()
                    startY = event.rawY.roundToInt()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = dragX + (event.rawX.roundToInt() - startX)
                    params.y = dragY + (event.rawY.roundToInt() - startY)
                    windowManager.updateViewLayout(view, params)
                    true
                }
                else -> false
            }
        }

        toggle.setOnClickListener {
            minimized = !minimized
            label.visibility = if (minimized) View.GONE else View.VISIBLE
            subView?.visibility = if (minimized) View.GONE else View.VISIBLE
            toggle.setImageResource(
                if (minimized) R.drawable.ic_expand else R.drawable.ic_collapse
            )
        }

        overlayView = view
        layoutParams = params
        windowManager.addView(view, params)

        val snap = repository.snapshot()
        render(snap.todayScrolls, snap.todayDistancePx)
    }

    private fun render(scrolls: Int, distancePx: Long) {
        countView?.text = scrolls.toString()
        subView?.text = getString(R.string.distance_format, formatDistance(distancePx))
    }

    private fun formatDistance(px: Long): String {
        return when {
            px < 1_000 -> "$px px"
            px < 100_000 -> String.format("%.1fk px", px / 1000.0)
            else -> "${px / 1000}k px"
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).roundToInt()

    override fun onDestroy() {
        overlayView?.let { windowManager.removeView(it) }
        overlayView = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_UPDATE_COUNT = "com.synectron.instascroll.UPDATE_COUNT"
        const val ACTION_STOP = "com.synectron.instascroll.STOP_OVERLAY"
        const val EXTRA_SCROLLS = "scrolls"
        const val EXTRA_DISTANCE = "distance"
        private const val NOTIFICATION_ID = 42

        fun canDrawOverlays(context: Context): Boolean =
            Settings.canDrawOverlays(context)

        fun updateCount(context: Context, scrolls: Int, distancePx: Long) {
            if (!canDrawOverlays(context)) return
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_UPDATE_COUNT
                putExtra(EXTRA_SCROLLS, scrolls)
                putExtra(EXTRA_DISTANCE, distancePx)
            }
            context.startForegroundService(intent)
        }

        fun start(context: Context) {
            context.startForegroundService(Intent(context, OverlayService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, OverlayService::class.java))
        }
    }
}
