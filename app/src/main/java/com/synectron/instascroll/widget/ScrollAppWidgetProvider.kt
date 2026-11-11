package com.synectron.instascroll.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.synectron.instascroll.R
import com.synectron.instascroll.data.ScrollRepository
import com.synectron.instascroll.ui.MainActivity

class ScrollAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val repository = ScrollRepository(context)
        val snap = repository.snapshot()
        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_scroll_count).apply {
                setTextViewText(R.id.widget_count, snap.todayScrolls.toString())
                setTextViewText(
                    R.id.widget_sub,
                    context.getString(R.string.widget_sub_today)
                )
                val open = PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_root, open)
            }
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_WIDGET_REFRESH ||
            intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE
        ) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, ScrollAppWidgetProvider::class.java)
            )
            onUpdate(context, manager, ids)
        }
    }

    companion object {
        const val ACTION_WIDGET_REFRESH = "com.synectron.instascroll.ACTION_WIDGET_REFRESH"

        fun requestUpdate(context: Context) {
            val intent = Intent(context, ScrollAppWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_REFRESH
            }
            context.sendBroadcast(intent)
        }
    }
}
