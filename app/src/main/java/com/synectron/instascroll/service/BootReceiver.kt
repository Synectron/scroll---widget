package com.synectron.instascroll.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.synectron.instascroll.data.ScrollRepository

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val repository = ScrollRepository(context)
        if (!repository.isOverlayEnabled() || !OverlayService.canDrawOverlays(context)) return
        try {
            OverlayService.start(context)
        } catch (_: Exception) {
            // Android may block FGS start from boot; overlay starts again from MainActivity.
        }
    }
}
