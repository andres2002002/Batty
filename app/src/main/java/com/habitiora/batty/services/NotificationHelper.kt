package com.habitiora.batty.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.habitiora.batty.MainActivity
import com.habitiora.batty.R
import com.habitiora.batty.domain.model.BatteryInfo
import com.habitiora.batty.domain.model.BatteryPlugged
import com.habitiora.batty.domain.model.BatteryStatus
import com.habitiora.batty.domain.model.DndBypassState
import com.habitiora.batty.domain.model.ThresholdEvent
import com.habitiora.batty.domain.model.ThresholdState
import com.habitiora.batty.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import timber.log.Timber
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val monitorSettingsRepository: SettingsRepository
) {
    companion object {
        // Canales — sufijo _v2 evita conflictos con versiones previas en el mismo dispositivo
        const val FOREGROUND_CHANNEL_ID = "battery_foreground"
        const val ALERTS_CHANNEL_ID = "battery_alerts"
        const val CRITICAL_CHANNEL_ID = "battery_critical"

        const val FOREGROUND_NOTIFICATION_ID = 1001
        const val ALERT_NOTIFICATION_ID = 1002
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init { createChannels() }

    private fun createChannels() {
        NotificationChannel(
            FOREGROUND_CHANNEL_ID,
            "Battery Monitor",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows current battery level while monitoring is active"
            setShowBadge(false)
            enableVibration(false)
            enableLights(false)
        }.also { notificationManager.createNotificationChannel(it) }

        NotificationChannel(
            ALERTS_CHANNEL_ID,
            "Battery Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifies when battery reaches configured thresholds"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 200)
        }.also { notificationManager.createNotificationChannel(it) }

        NotificationChannel(
            CRITICAL_CHANNEL_ID,
            "Critical Battery",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Urgent alerts for critically low battery levels"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 150, 300)
            //setBypassDnd(true)
        }.also { notificationManager.createNotificationChannel(it) }
    }

    // ─────────────────────────────────────────────────────────────────
    // DND bypass — estado real desde el sistema
    // ─────────────────────────────────────────────────────────────────

    fun getDndBypassState(): DndBypassState = DndBypassState(
        channelCanBypass = notificationManager
            .getNotificationChannel(CRITICAL_CHANNEL_ID)
            ?.canBypassDnd() ?: false
    )


    // ─────────────────────────────────────────────────────────────────
    // Foreground / Persistent
    // ─────────────────────────────────────────────────────────────────

    fun buildForegroundNotification(
        info: BatteryInfo,
        thresholdState: ThresholdState
    ): Notification {
        val titleText = buildForegroundTitle(info)
        val contentText = buildForegroundText(info, thresholdState)
        val expandedHtml = buildExpandedBody(info, thresholdState)
        val dynamicIcon = R.drawable.batty_icon_open

        return NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
            .setContentTitle(
                HtmlCompat.fromHtml(
                    "<b>$titleText</b>",
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
            )
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(HtmlCompat.fromHtml(expandedHtml, HtmlCompat.FROM_HTML_MODE_COMPACT))
            )
            .setSmallIcon(dynamicIcon)
            .setProgress(100, info.level, false)
            .setSubText(info.technology)
            .setContentIntent(mainActivityPendingIntent())
            .addAction(0, "Stop monitoring", stopServicePendingIntent())
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setLocalOnly(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    // ─────────────────────────────────────────────────────────────────
    // Threshold Alerts
    // ─────────────────────────────────────────────────────────────────

    suspend fun showThresholdAlert(event: ThresholdEvent) {
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        if (!hasPermission) {
            Timber.w("POST_NOTIFICATIONS not granted — alert skipped")
            return
        }

        val settings = monitorSettingsRepository.get()

        if (settings.alertPolicy.isDisabled) {
            Timber.d("notifyBattery=false — alert suppressed")
            return
        }

        val content = resolveAlertContent(event)

        // onlyCriticalAlerts: descartar si el canal no es crítico
        if (settings.alertPolicy.isOnlyCritical && content.channelId != CRITICAL_CHANNEL_ID) {
            Timber.d("Alert suppressed (onlyCriticalAlerts=true): ${content.title}")
            return
        }
        val notification = buildAlertNotification(content, event)
        // val notification = buildAlertNotification(content)
        NotificationManagerCompat.from(context).notify(ALERT_NOTIFICATION_ID, notification)
        Timber.i("Alert shown: ${content.title}")
    }

    private fun buildAlertNotification(
        content: AlertContent,
        event: ThresholdEvent
    ): Notification {
        val builder = NotificationCompat.Builder(context, content.channelId)
            .setContentTitle(
                HtmlCompat.fromHtml(
                    "<b>${content.title}</b>",
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
            )
            .setContentText(content.summary)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(content.body)
                    .setBigContentTitle(
                        HtmlCompat.fromHtml(
                            "<b>${content.title}</b>",
                            HtmlCompat.FROM_HTML_MODE_COMPACT
                        )
                    )
            )
            .setSmallIcon(content.iconRes)
            .setPriority(content.priority)
            .setAutoCancel(true)
            .setCategory(content.category)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(mainActivityPendingIntent())

        return builder.build()
    }

    fun notify(id: Int, notification: Notification) =
        notificationManager.notify(id, notification)

    // ─────────────────────────────────────────────────────────────────
    // Foreground Formatters
    // ─────────────────────────────────────────────────────────────────

    private fun buildForegroundTitle(info: BatteryInfo): String {
        val statusLabel = when {
            info.status == BatteryStatus.FULL -> "Fully charged"
            info.isCharging -> "Charging"
            else -> "Battery"
        }
        return "$statusLabel • ${info.level}%"
    }

    private fun buildForegroundText(info: BatteryInfo, state: ThresholdState): String = when {
        state.hasActiveHighTrigger -> "Charge target reached • ${info.plugged.label}"
        state.hasActiveLowTrigger -> "Low battery • ${info.plugged.label}"
        else -> "${info.status.label} • ${info.plugged.label}"
    }

    private fun buildExpandedBody(info: BatteryInfo, state: ThresholdState): String = buildString {
        append("<b>Status:</b> ${info.status.label} &nbsp;•&nbsp; <b>Plug:</b> ${info.plugged.label}<br>")
        append("<b>Temp:</b> ${"%.1f".format(info.temperature)}°C &nbsp;•&nbsp; <b>Voltage:</b> ${info.voltage} mV<br>")
        append("<b>Health:</b> ${info.health.name}")

        if (info.watts > 0f) {
            append("<br><b>Power:</b> ${"%.1f".format(info.watts)} W")
        }

        if (state.hasActiveTrigger) {
            append("<br><br><i>")
            append(
                if (state.hasActiveLowTrigger) "⚠️ Low battery threshold active"
                else "✅ Charge threshold reached"
            )
            append("</i>")
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Alert Content Resolvers
    // ─────────────────────────────────────────────────────────────────

    private data class AlertContent(
        val title: String,
        val summary: String,
        val body: String,
        val channelId: String,
        val priority: Int,
        val category: String,
        val iconRes: Int,
        val largeIconRes: Int? = null
    )

    private fun resolveAlertContent(event: ThresholdEvent): AlertContent = when (event) {
        is ThresholdEvent.LowBattery -> resolveLowContent(event)
        is ThresholdEvent.HighBattery -> resolveHighContent(event)
    }

    private fun resolveLowContent(event: ThresholdEvent.LowBattery): AlertContent = when {
        event.isCritical -> AlertContent(
            title = "Battery critically low • ${event.level}%",
            summary = "Connect a charger to avoid shutdown.",
            body = "Battery is at ${event.level}%. Connect a charger immediately to prevent an unexpected shutdown.",
            channelId = CRITICAL_CHANNEL_ID,
            priority = NotificationCompat.PRIORITY_MAX,
            category = NotificationCompat.CATEGORY_ALARM,
            iconRes = R.drawable.batty_icon_24p,
            largeIconRes = R.drawable.batty_icon_close
        )

        event.isVeryLow -> AlertContent(
            title = "Battery very low • ${event.level}%",
            summary = "Connect a charger soon.",
            body = "Battery is at ${event.level}%. Your device will shut down soon if not connected to power.",
            channelId = CRITICAL_CHANNEL_ID,
            priority = NotificationCompat.PRIORITY_HIGH,
            category = NotificationCompat.CATEGORY_ALARM,
            iconRes = R.drawable.batty_icon_24p,
            largeIconRes = R.drawable.batty_icon_close
        )

        else -> AlertContent(
            title = "Battery low • ${event.level}%",
            summary = "Consider charging your device.",
            body = "Battery level is at ${event.level}%. Connect a charger when convenient to keep your device running.",
            channelId = ALERTS_CHANNEL_ID,
            priority = NotificationCompat.PRIORITY_DEFAULT,
            category = NotificationCompat.CATEGORY_REMINDER,
            iconRes = R.drawable.baseline_notifications_active_24
        )
    }

    private fun resolveHighContent(event: ThresholdEvent.HighBattery): AlertContent =
        if (event.isFullyCharged) {
            AlertContent(
                title = "Battery fully charged",
                summary = "You can unplug the charger.",
                body = "Your battery is fully charged. Unplugging now helps preserve long-term battery health.",
                channelId = ALERTS_CHANNEL_ID,
                priority = NotificationCompat.PRIORITY_DEFAULT,
                category = NotificationCompat.CATEGORY_REMINDER,
                iconRes = R.drawable.batty_icon_open,
                largeIconRes = R.drawable.batty_icon_close
            )
        } else {
            AlertContent(
                title = "Battery charged to ${event.level}%",
                summary = "Recommended charge level reached.",
                body = "Battery has reached ${event.level}%. For optimal battery longevity, consider unplugging now.",
                channelId = ALERTS_CHANNEL_ID,
                priority = NotificationCompat.PRIORITY_DEFAULT,
                category = NotificationCompat.CATEGORY_REMINDER,
                iconRes = R.drawable.batty_icon_24p
            )
        }

    // ─────────────────────────────────────────────────────────────────
    // Utils
    // ─────────────────────────────────────────────────────────────────
    private fun mainActivityPendingIntent(): PendingIntent = PendingIntent.getActivity(
        context, 0,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun stopServicePendingIntent(): PendingIntent = PendingIntent.getService(
        context, 1,
        Intent(context, BatteryMonitorService::class.java).apply {
            action = BatteryMonitorService.ACTION_STOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}