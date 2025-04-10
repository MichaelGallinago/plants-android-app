package net.micg.plantcare.receiver.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import net.micg.plantcare.receiver.alarm.AlarmReceiver.Companion.ID_EXTRA
import net.micg.plantcare.receiver.alarm.AlarmReceiver.Companion.NAME_EXTRA
import net.micg.plantcare.receiver.alarm.AlarmReceiver.Companion.TYPE_EXTRA
import net.micg.plantcare.utils.FirebaseUtils
import net.micg.plantcare.utils.FirebaseUtils.onNotificationDismissed

class NotificationDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) =
        onNotificationDismissed(context, intent)
}
