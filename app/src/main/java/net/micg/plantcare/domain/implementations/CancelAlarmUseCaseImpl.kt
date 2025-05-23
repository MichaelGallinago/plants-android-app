package net.micg.plantcare.domain.implementations

import android.content.Context
import net.micg.plantcare.domain.useCase.CancelAlarmUseCase
import net.micg.plantcare.receiver.alarm.AlarmNotificationUtils
import javax.inject.Inject

class CancelAlarmUseCaseImpl @Inject constructor(
    private val context: Context
) : CancelAlarmUseCase {
    override operator fun invoke(id: Long) = AlarmNotificationUtils.cancelAlarm(context, id)
}
