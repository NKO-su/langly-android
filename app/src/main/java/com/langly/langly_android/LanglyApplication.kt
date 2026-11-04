package com.langly.langly_android

import android.app.Application
import com.langly.langly_android.network.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LanglyApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        TokenManager.init(applicationContext)

        appScope.launch {
            val start = System.currentTimeMillis()
        }
    }
}