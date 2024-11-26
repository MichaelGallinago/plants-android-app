package net.micg.plantcare.domain.implementations

import android.content.Context
import net.micg.plantcare.domain.usecase.SetAlarmUseCase
import net.micg.plantcare.domain.utils.AlarmNotificationUtils
import net.micg.plantcare.presentation.models.Alarm
import javax.inject.Inject

class SetAlarmUseCaseImpl @Inject constructor(private val context: Context) : SetAlarmUseCase {
    override operator fun invoke(alarm: Alarm) = AlarmNotificationUtils.setAlarm(context, alarm)
}
