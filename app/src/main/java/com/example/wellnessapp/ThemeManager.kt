package com.example.wellnessapp

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView

object ThemeManager {
    fun applyTheme(activity: Activity) {
        val settingsPref = activity.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val bgColor = settingsPref.getInt("theme_bg", Color.BLACK)
        val accentColor = settingsPref.getInt("theme_accent", Color.WHITE)
        
        val rootLayout = activity.window.decorView.findViewById<View>(android.R.id.content)
        rootLayout.setBackgroundColor(bgColor)
        
        // Apply Glass Effect to all CardViews
        applyGlassToView(rootLayout, accentColor)
    }

    private fun applyGlassToView(view: View, accentColor: Int) {
        if (view is CardView) {
            val glassAlpha = Color.argb(30, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor))
            view.setCardBackgroundColor(glassAlpha)
            view.cardElevation = 0f
            
            // Add a thin border for "morphic" look
            val stroke = GradientDrawable()
            stroke.setColor(glassAlpha)
            stroke.setStroke(2, Color.argb(60, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor)))
            stroke.cornerRadius = view.radius
            view.background = stroke
        }
        
        if (view is BottomNavigationView) {
            val settingsPref = view.context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
            view.setBackgroundColor(settingsPref.getInt("theme_bg", Color.BLACK))
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyGlassToView(view.getChildAt(i), accentColor)
            }
        }
    }
}
