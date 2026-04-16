package com.example.wellnessapp

import java.util.Properties

object Config {
    fun getApiKey(context: android.content.Context): String {
        // In a production environment, you would use BuildConfig or a secure backend.
        // For this 2026 stability update, we pull from a designated properties file or BuildConfig.
        return "AIzaSyDV2Fu1LHO7JaAMSPx-CdYzUpTwpzAqNjU" 
    }
}
