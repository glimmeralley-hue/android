package com.example.wellnessapp

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import android.widget.Button

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        ThemeManager.applyTheme(this)

        val viewPager = findViewById<ViewPager2>(R.id.viewPagerThemes)
        
        val themes = listOf(
            ThemeModel("Neon Glass", "High-energy cyber aesthetics with neon highlights.", 0xFF000000.toInt(), 0xFF39FF14.toInt(), true),
            ThemeModel("Frost Blue", "Crisp, cold translucent arctic vibes.", 0xFF001F3F.toInt(), 0xFF7FDBFF.toInt(), true),
            ThemeModel("Rose Quartz", "Soft, elegant glass with a warm pink hue.", 0xFF1A0F0F.toInt(), 0xFFFF69B4.toInt(), true),
            ThemeModel("Deep Space", "Infinite darkness with starlight white accents.", 0xFF050505.toInt(), 0xFFE0E0E0.toInt(), true),
            ThemeModel("Cyber Sunset", "Vibrant purple and orange cybernetic fusion.", 0xFF120412.toInt(), 0xFFFF00FF.toInt(), true),
            ThemeModel("Bio-Hazard", "Toxic yellow on industrial carbon gray.", 0xFF1A1A1A.toInt(), 0xFFDFFF00.toInt(), true),
            ThemeModel("Void", "Absolute minimalism. Pure black, pure white.", 0xFF000000.toInt(), 0xFFFFFFFF.toInt(), true),
            ThemeModel("Solar Flare", "Intense orange energy on deep solarized teal.", 0xFF073642.toInt(), 0xFFCB4B16.toInt(), true),
            ThemeModel("Emerald City", "Rich greens and glass for a lush digital forest.", 0xFF061A06.toInt(), 0xFF50C878.toInt(), true),
            ThemeModel("Amethyst", "Deep purple crystal glass for mystical precision.", 0xFF140B1A.toInt(), 0xFF9966CC.toInt(), true)
        )

        val adapter = ThemeAdapter(themes) { selectedTheme ->
            val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putInt("theme_bg", selectedTheme.backgroundColor)
                putInt("theme_accent", selectedTheme.accentColor)
                apply()
            }
            // Apply immediately to current activity to show preview
            ThemeManager.applyTheme(this)
            // finish() // Remove auto-finish so user can see the change
        }

        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1

        findViewById<Button>(R.id.btnBackSettings).setOnClickListener { finish() }
    }
}
