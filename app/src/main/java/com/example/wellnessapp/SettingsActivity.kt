package com.example.wellnessapp

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import android.widget.Button

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val viewPager = findViewById<ViewPager2>(R.id.viewPagerThemes)
        
        val themes = listOf(
            ThemeModel("Stark", "Pure monochrome precision.", 0xFF000000.toInt(), 0xFFFFFFFF.toInt(), true),
            ThemeModel("Glass", "Translucent frost aesthetics.", 0xFF121212.toInt(), 0x1AFFFFFF.toInt(), true),
            ThemeModel("Midnight", "Deep navy and soft grays.", 0xFF0B0E14.toInt(), 0xFF48484A.toInt(), true),
            ThemeModel("Aurora", "Subtle gradients and energy.", 0xFF000000.toInt(), 0xFF39FF14.toInt(), true),
            ThemeModel("Solar", "High-contrast solarized amber.", 0xFF073642.toInt(), 0xFF2AA198.toInt(), true),
            ThemeModel("Carbon", "Industrial dark gray and orange.", 0xFF1A1A1B.toInt(), 0xFFFF4500.toInt(), true),
            ThemeModel("Ethereal", "Soft lavender and deep purple.", 0xFF1A132F.toInt(), 0xFF967EBD.toInt(), true),
            ThemeModel("Oceanic", "Deep sea blues and teal.", 0xFF001F3F.toInt(), 0xFF7FDBFF.toInt(), true)
        )

        val adapter = ThemeAdapter(themes) { selectedTheme ->
            val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putInt("theme_bg", selectedTheme.backgroundColor)
                putInt("theme_accent", selectedTheme.accentColor)
                apply()
            }
            finish()
        }

        viewPager.adapter = adapter
        
        viewPager.offscreenPageLimit = 1
        viewPager.setPageTransformer { page, position ->
            val pageMargin = 40
            val pageOffset = 20
            val myOffset = position * -(2 * pageOffset + pageMargin)
            page.translationX = myOffset
        }

        findViewById<Button>(R.id.btnBackSettings).setOnClickListener { finish() }
    }
}
