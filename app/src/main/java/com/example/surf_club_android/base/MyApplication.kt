package com.example.surf_club_android.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class MyApplication: Application() {
    @SuppressLint("StaticFieldLeak")
    object Globals {
        var context: Context? = null
    }
    override fun onCreate() {
        super.onCreate()
        Globals.context = applicationContext
    }
}