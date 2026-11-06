package com.byd.workout

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.byd.workout.data.LogRepository
import com.byd.workout.data.PlanRepository
import com.byd.workout.data.Seed
import com.byd.workout.data.SessionRepository
import com.byd.workout.data.db.BydDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppContainer(context: Context) {
    val appContext: Context = context.applicationContext
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val settingsDataStore = context.settingsDataStore
    val database = BydDatabase.create(context)

    val planRepository = PlanRepository(database.planDao())
    val logRepository = LogRepository(database.logDao())
    val sessionRepository = SessionRepository(settingsDataStore)
}

class BydApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.applicationScope.launch {
            Seed.seedIfNeeded(
                planDao = container.database.planDao(),
                logRepository = container.logRepository,
                settings = container.settingsDataStore,
                includeSampleHistory = BuildConfig.DEBUG,
            )
        }
    }
}
