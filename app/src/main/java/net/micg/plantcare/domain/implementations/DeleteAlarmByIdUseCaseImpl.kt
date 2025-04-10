package net.micg.plantcare.domain.implementations

import net.micg.plantcare.data.alarm.repository.AlarmsRepository
import net.micg.plantcare.domain.useCase.DeleteAlarmByIdUseCase
import javax.inject.Inject

class DeleteAlarmByIdUseCaseImpl @Inject constructor(
    private val repository: AlarmsRepository
) : DeleteAlarmByIdUseCase {
    override suspend operator fun invoke(id: Long) = repository.deleteById(id)
}
