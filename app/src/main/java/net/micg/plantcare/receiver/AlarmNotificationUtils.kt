package net.micg.plantcare.receiver

import android.app.AlarmManager
import android.app.PendingIntent.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

object AlarmNotificationUtils {
    private const val HALF_MINUTE_IN_MILLIS = 1000L * 30L

    fun cancelAlarm(context: Context, id: Long) = getAlarmManager(context).cancel(
        createPendingIntent(context, id.toInt(), createIntent(context))
    )

    fun getValidDate(date: Long, interval: Long): Long {
        val currentTime = System.currentTimeMillis()
        if (date >= currentTime - HALF_MINUTE_IN_MILLIS) return date

        return date + ((currentTime - date) / interval + 1L) * interval
    }

    fun setAlarm(
        context: Context,
        id: Int,
        name: String,
        type: String,
        dateInMillis: Long,
        intervalInMillis: Long,
    ) {
        while (!canScheduleExactAlarms(context)) {
            requestExactAlarmPermission(context)
        }

        getAlarmManager(context).setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            dateInMillis,
            createPendingIntent(
                context,
                id,
                createExtraIntent(context, id, name, type, dateInMillis, intervalInMillis)
            )
        )
    }

    private fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    private fun requestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

        context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun createPendingIntent(context: Context, id: Int, intent: Intent) = getBroadcast(
        context,
        id,
        intent,
        FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
    )

    private fun createIntent(context: Context) = Intent(context, AlarmReceiver::class.java)

    private fun createExtraIntent(
        context: Context,
        id: Int,
        name: String,
        type: String,
        dateInMillis: Long,
        intervalInMillis: Long,
    ) = createIntent(context).apply {
        putExtra(AlarmReceiver.Companion.ALARM_ID, id)
        putExtra(AlarmReceiver.Companion.ALARM_NAME, name)
        putExtra(AlarmReceiver.Companion.ALARM_TYPE, type)
        putExtra(AlarmReceiver.Companion.ALARM_DATE, dateInMillis)
        putExtra(AlarmReceiver.Companion.ALARM_INTERVAL, intervalInMillis)
    }

    private fun getAlarmManager(context: Context) =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
}
