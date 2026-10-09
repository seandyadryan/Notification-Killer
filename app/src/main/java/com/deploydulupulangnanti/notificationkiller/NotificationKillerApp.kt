package com.deploydulupulangnanti.notificationkiller

import android.app.Application
import com.deploydulupulangnanti.notificationkiller.data.local.AppDatabase
import com.deploydulupulangnanti.notificationkiller.data.local.preferences.PreferenceManager
import com.deploydulupulangnanti.notificationkiller.data.repository.NotificationKillerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationKillerApp : Application() {
    lateinit var repository: NotificationKillerRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = NotificationKillerRepository(AppDatabase.getInstance(this), PreferenceManager(this))
        CoroutineScope(Dispatchers.IO).launch { repository.purgeOldHistory(7) }
    }
}
