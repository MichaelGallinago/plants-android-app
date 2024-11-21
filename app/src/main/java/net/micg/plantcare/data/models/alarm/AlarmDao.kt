package net.micg.plantcare.data.models.alarm

import androidx.room.*

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: AlarmEntity): Long

    @Query("DELETE FROM alarms WHERE id = :alarmId")
    suspend fun deleteById(alarmId: Long)

    @Query("SELECT * FROM alarms ORDER BY id")
    suspend fun getAll(): List<AlarmEntity>

    @Query("UPDATE alarms SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun update(isEnabled: Boolean, id: Long)
}
