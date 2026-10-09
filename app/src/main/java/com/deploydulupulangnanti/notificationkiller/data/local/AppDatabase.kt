package com.deploydulupulangnanti.notificationkiller.data.local

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

@Database(entities = [RuleEntity::class, AppFilterEntity::class, HistoryEntity::class], version = 2, exportSchema = false)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ruleDao(): RuleDao
    abstract fun appFilterDao(): AppFilterDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "notification_killer.db")
                .addMigrations(MIGRATION_1_2)
                .build().also { INSTANCE = it }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS notification_history_new (eventId TEXT NOT NULL, packageName TEXT NOT NULL, appName TEXT NOT NULL, matchedRule TEXT NOT NULL, actionTaken TEXT NOT NULL, result TEXT NOT NULL, timestamp INTEGER NOT NULL, PRIMARY KEY(eventId))")
                db.execSQL("INSERT OR IGNORE INTO notification_history_new (eventId, packageName, appName, matchedRule, actionTaken, result, timestamp) SELECT CAST(id AS TEXT), packageName, appName, matchedRule, actionTaken, 'UNKNOWN', timestamp FROM notification_history")
                db.execSQL("DROP TABLE notification_history")
                db.execSQL("ALTER TABLE notification_history_new RENAME TO notification_history")
            }
        }
    }
}
