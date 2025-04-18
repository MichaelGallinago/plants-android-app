package net.micg.plantcare.data.alarm.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.micg.plantcare.data.alarm.models.AlarmEntity

@Dao
interface AlarmDao {
    @Update
    suspend fun updateData(alarm: AlarmEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: AlarmEntity): Long

    @Query("DELETE FROM alarms WHERE id = :alarmId")
    suspend fun deleteById(alarmId: Long)

    @Query("UPDATE alarms SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun update(isEnabled: Boolean, id: Long)

    @Query("SELECT * FROM alarms ORDER BY id")
    fun getAll(): Flow<List<AlarmEntity>>
}
