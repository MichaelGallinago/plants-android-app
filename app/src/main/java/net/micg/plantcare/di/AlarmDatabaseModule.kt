package net.micg.plantcare.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import net.micg.plantcare.data.db.alarm.AlarmDatabase

@Module
class AlarmDatabaseModule {
    @Provides
    fun provideDatabase(context: Context) = Room.databaseBuilder(
        context.applicationContext, AlarmDatabase::class.java, "alarm_database"
    )
        .fallbackToDestructiveMigration() //TODO: REMOVE THIS
        .build()

    @Provides
    fun provideAlarmDao(database: AlarmDatabase) = database.alarmDao()
}
