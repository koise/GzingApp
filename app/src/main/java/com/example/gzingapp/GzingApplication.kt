package com.example.gzingapp

import android.app.Application
import com.example.gzingapp.network.RetrofitClient

class GzingApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize RetrofitClient with application context
        RetrofitClient.initialize(this)
    }
}

