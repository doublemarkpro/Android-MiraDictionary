package com.handdict.studyassistant.timer

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.handdict.studyassistant.MainActivity
import com.handdict.studyassistant.R

object TimerAlarmScheduler {
    private const val REQUEST_CODE = 4107
    private const val EXTRA_TASK = "task"
    private const val EXTRA_DURATION = "duration"
    private const val EXTRA_END_AT = "end_at"

    @SuppressLint("ScheduleExactAlarm", "MissingPermission")
    fun schedule(context: Context, taskName: String, durationMinutes: Int, endAtMillis: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val operation = pendingIntent(context, taskName, durationMinutes, endAtMillis)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAtMillis, operation)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endAtMillis, operation)
        }
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(pendingIntent(context, "", 0, 0L))
    }

    private fun pendingIntent(
        context: Context,
        taskName: String,
        durationMinutes: Int,
        endAtMillis: Long,
    ): PendingIntent {
        val intent = Intent(context, TimerAlarmReceiver::class.java).apply {
            putExtra(EXTRA_TASK, taskName)
            putExtra(EXTRA_DURATION, durationMinutes)
            putExtra(EXTRA_END_AT, endAtMillis)
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    internal fun readTask(intent: Intent): String = intent.getStringExtra(EXTRA_TASK).orEmpty()
    internal fun readDuration(intent: Intent): Int = intent.getIntExtra(EXTRA_DURATION, 0)
    internal fun readEndAt(intent: Intent): Long = intent.getLongExtra(EXTRA_END_AT, 0L)
}

class TimerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val task = TimerAlarmScheduler.readTask(intent).ifBlank { "本轮作业" }
        val duration = TimerAlarmScheduler.readDuration(intent).coerceAtLeast(1)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = context.getSystemService(NotificationManager::class.java)
        val channelId = "mira_focus_timer"
        manager.createNotificationChannel(
            NotificationChannel(channelId, "作业计时提醒", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "作业计时结束时提醒"
            }
        )
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_timer_notification)
            .setContentTitle("已达到预计完成时间")
            .setContentText("“$task”已计时 $duration 分钟，可继续计时或返回应用完成作业。")
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
        manager.notify(4107, notification)
    }
}
