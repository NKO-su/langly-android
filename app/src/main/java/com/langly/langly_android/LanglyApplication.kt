package com.langly.langly_android

import android.app.Application
import com.langly.langly_android.network.TokenManager

class LanglyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenManager.init(applicationContext)
    }
}