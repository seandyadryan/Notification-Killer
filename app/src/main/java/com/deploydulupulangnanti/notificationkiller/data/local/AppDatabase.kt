package com.deploydulupulangnanti.notificationkiller.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.deploydulupulangnanti.notificationkiller.data.local.dao.*
import com.deploydulupulangnanti.notificationkiller.data.local.entity.*

@Database(entities = [RuleEntity::class, AppFilterEntity::class, HistoryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ruleDao(): RuleDao
    abstract fun appFilterDao(): AppFilterDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "notification_killer.db")
                .fallbackToDestructiveMigration().build().also { INSTANCE = it }
        }
    }
}
