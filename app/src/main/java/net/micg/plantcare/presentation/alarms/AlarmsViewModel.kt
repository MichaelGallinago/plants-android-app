package net.micg.plantcare.presentation.alarms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.micg.plantcare.domain.implementations.CancelAlarmUseCaseImpl
import net.micg.plantcare.domain.implementations.DeleteAlarmByIdUseCaseImpl
import net.micg.plantcare.domain.implementations.GetAllAlarmsUseCaseImpl
import net.micg.plantcare.domain.implementations.SetAlarmUseCaseImpl
import net.micg.plantcare.domain.implementations.UpdateAlarmUseCaseImpl
import net.micg.plantcare.presentation.models.Alarm
import javax.inject.Inject

class AlarmsViewModel @Inject constructor(
    private val deleteAlarmUseCase: DeleteAlarmByIdUseCaseImpl,
    private val getAllAlarmsUseCase: GetAllAlarmsUseCaseImpl,
    private val updateAlarmUseCase: UpdateAlarmUseCaseImpl,
    private val setAlarmUseCase: SetAlarmUseCaseImpl,
    private val cancelAlarmUseCase: CancelAlarmUseCaseImpl,
) : ViewModel() {
    private val _allAlarms = MutableLiveData<List<Alarm>>()
    val allAlarms: LiveData<List<Alarm>> get() = _allAlarms

    fun refreshAlarms() = viewModelScope.launch(Dispatchers.IO) {
        _allAlarms.postValue(getAllAlarmsUseCase())
    }

    fun delete(alarm: Alarm) = viewModelScope.launch(Dispatchers.IO) {
        deleteAlarmUseCase(alarm.id)
        cancelAlarmUseCase(alarm.id)
        refreshAlarms()
    }

    fun update(isEnabled: Boolean, alarm: Alarm) {
        viewModelScope.launch(Dispatchers.IO) {
            updateAlarmUseCase(isEnabled, alarm.id)
            refreshAlarms()
        }

        if (isEnabled) {
            setAlarmUseCase(alarm)
        } else {
            cancelAlarmUseCase(alarm.id)
        }
    }
}
