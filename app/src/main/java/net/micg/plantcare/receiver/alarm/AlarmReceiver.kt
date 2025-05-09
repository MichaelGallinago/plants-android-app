package net.micg.plantcare.receiver.alarm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.micg.plantcare.PlantCareApplication
import net.micg.plantcare.R
import net.micg.plantcare.domain.useCase.DeleteAlarmByIdUseCase
import net.micg.plantcare.receiver.alarm.AlarmNotificationUtils.createContentIntent
import net.micg.plantcare.receiver.alarm.AlarmNotificationUtils.createDeleteIntent
import net.micg.plantcare.receiver.alarm.AlarmNotificationUtils.getGroupSummaryNotification
import net.micg.plantcare.utils.AlarmCreationUtils
import net.micg.plantcare.utils.FirebaseUtils
import net.micg.plantcare.utils.FirebaseUtils.POSTED_PUSH_MESSAGES
import javax.inject.Inject
import kotlin.random.Random

class AlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var deleteAlarmUseCase: DeleteAlarmByIdUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(ALARM_ID, 0)

        val name = getString(context, intent, ALARM_NAME, R.string.notification_name_placeholder)
        val type = getString(context, intent, ALARM_TYPE, R.string.notification_type_placeholder)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (notificationManager.activeNotifications.count()) {
            0 -> notifyFirst(context, notificationManager, id, name, type)
            1 -> notifySecondAndUpdateFirst(context, notificationManager, id, name, type)
            else -> {
                notifyAlarm(context, notificationManager, id, name, type)
                notifyGroupSummary(context, notificationManager)
            }
        }
        FirebaseUtils.onNotificationEvent(context, POSTED_PUSH_MESSAGES, intent)

        val dateInMillis = intent.getLongExtra(ALARM_DATE, System.currentTimeMillis())

        val intervalInMillis =
            intent.getLongExtra(ALARM_INTERVAL, AlarmCreationUtils.calculateIntervalInMillis(1L))

        if (intervalInMillis == 0L)
        {
            (context.applicationContext as PlantCareApplication).appComponent.inject(this)
            CoroutineScope(Dispatchers.IO).launch {
                deleteAlarmUseCase(id.toLong())
            }
            return
        }

        AlarmNotificationUtils.setAlarm(
            context, id, name, type, dateInMillis + intervalInMillis, intervalInMillis
        )
    }

    private fun notifyFirst(
        context: Context,
        notificationManager: NotificationManager,
        id: Int,
        name: String,
        type: String
    ) = notificationManager.notify(
        id,
        NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_flower)
            .setContentTitle(name)
            .setContentText(type)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(createContentIntent(context, id))
            .setAutoCancel(false)
            .setOngoing(true)
            .setGroup(ALARM_GROUP)
            .setDeleteIntent(createDeleteIntent(context, id, name, type))
            .addAction(
                R.drawable.ic_close,
                context.getString(R.string.complete),
                PendingIntent.getBroadcast(
                    context,
                    id,
                    Intent(context, NotificationCloseReceiver::class.java).apply {
                        putExtra("id", id)
                        putExtra("name", id)
                        putExtra("type", id)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    )

    private fun notifySecondAndUpdateFirst(
        context: Context,
        notificationManager: NotificationManager,
        id: Int,
        name: String,
        type: String
    ) {
        val existsNotification = notificationManager.activeNotifications[0]
        val extras = existsNotification.notification.extras
        notifyAlarm(
            context,
            notificationManager,
            existsNotification.id,
            extras.getString(Notification.EXTRA_TITLE) ?: "",
            extras.getString(Notification.EXTRA_TEXT) ?: ""
        )
        notifyAlarm(context, notificationManager, id, name, type)
        notifyGroupSummary(context, notificationManager)
    }

    private fun notifyAlarm(
        context: Context,
        notificationManager: NotificationManager,
        id: Int,
        name: String,
        type: String
    ) = notificationManager.notify(
        id,
        NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_flower)
            .setContentTitle(name)
            .setContentText(type)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(createContentIntent(context, id))
            .setAutoCancel(false)
            .setGroup(ALARM_GROUP)
            .setDeleteIntent(createDeleteIntent(context, id, name, type))
            .build()
    )

    private fun notifyGroupSummary(
        context: Context, notificationManager: NotificationManager
    ) = Handler(Looper.getMainLooper()).postDelayed({
        notificationManager.notify(
            GROUP_SUMMARY_ID,
            getGroupSummaryNotification(
                context,
                context.getString(R.string.complete),
                context.getString(when(Random.nextInt(0, 3)) {
                    0 -> R.string.complete_title_1
                    1 -> R.string.complete_title_2
                    2 -> R.string.complete_title_3
                    else -> R.string.complete_title_3
                })
            )
        )
    }, GROUP_SUMMARY_DELAY)

    private fun getString(context: Context, intent: Intent, name: String, resId: Int) =
        intent.getStringExtra(name).takeUnless { it.isNullOrBlank() } ?: context.getString(resId)

    companion object {
        const val ID_EXTRA = "id"
        const val NAME_EXTRA = "name"
        const val TYPE_EXTRA = "type"

        const val FRAGMENT_TAG = "fragment_tag"
        const val ALARMS_FRAGMENT_TAG = "fragment_tag"

        const val ALARM_CHANNEL_ID = "flowers_alarm_channel_id"
        const val ALARM_ID = "ALARM_ID"
        const val ALARM_NAME = "ALARM_NAME"
        const val ALARM_TYPE = "ALARM_TYPE"
        const val ALARM_DATE = "ALARM_DATE"
        const val ALARM_INTERVAL = "ALARM_INTERVAL"
        const val ALARM_GROUP = "ALARM_GROUP"

        const val GROUP_SUMMARY_DELAY = 400L
        const val GROUP_SUMMARY_ID = Int.MIN_VALUE
    }
}
