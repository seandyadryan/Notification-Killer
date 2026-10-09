package com.deploydulupulangnanti.notificationkiller.data.local

import android.content.Context
import androidx.room.*
import com.deploydulupulangnanti.notificationkiller.data.local.dao.*
import com.deploydulupulangnanti.notificationkiller.data.local.entity.*
import com.deploydulupulangnanti.notificationkiller.domain.model.*

class RoomConverters {
    @TypeConverter fun fromRuleType(v: RuleType): String = v.name
    @TypeConverter fun toRuleType(v: String): RuleType = try { RuleType.valueOf(v) } catch (_: Exception) { RuleType.CONTAINS }

    @TypeConverter fun fromRuleScope(v: RuleScope): String = v.name
    @TypeConverter fun toRuleScope(v: String): RuleScope = try { RuleScope.valueOf(v) } catch (_: Exception) { RuleScope.TITLE_OR_TEXT }

    @TypeConverter fun fromActionType(v: ActionType): String = v.name
    @TypeConverter fun toActionType(v: String): ActionType = try { ActionType.valueOf(v) } catch (_: Exception) { ActionType.AUTO_DISMISS }
}

@Database(entities = [RuleEntity::class, AppFilterEntity::class, HistoryEntity::class], version = 1, exportSchema = false)
@TypeConverters(RoomConverters::class)
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
