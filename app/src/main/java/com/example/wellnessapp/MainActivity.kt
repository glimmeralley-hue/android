package com.example.wellnessapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val apiKey = "AIzaSyDV2Fu1LHO7JaAMSPx-CdYzUpTwpzAqNjU"
    private lateinit var auraNotifications: AuraNotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val settingsPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val bgColor = settingsPref.getInt("theme_bg", Color.BLACK)
        
        setContentView(R.layout.activity_main)
        
        findViewById<View>(android.R.id.content).setBackgroundColor(bgColor)

        auraNotifications = AuraNotificationManager(this)

        val profileCircle = findViewById<CardView>(R.id.profileCircle)
        profileCircle.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<CardView>(R.id.cardBiostats).setOnClickListener {
            startActivity(Intent(this, BiostatsActivity::class.java))
        }

        findViewById<CardView>(R.id.cardNutrition).setOnClickListener {
            startActivity(Intent(this, MainActivity2::class.java))
        }

        findViewById<CardView>(R.id.cardRecipes).setOnClickListener {
            startActivity(Intent(this, RecipesActivity::class.java))
        }

        findViewById<CardView>(R.id.cardRoutines).setOnClickListener {
            startActivity(Intent(this, PersonalizedActivity::class.java))
        }

        findViewById<CardView>(R.id.cardShop).setOnClickListener {
            startActivity(Intent(this, PRODUCTSActivity::class.java))
        }

        findViewById<android.widget.Button>(R.id.btnAddProductAdmin).setOnClickListener {
            startActivity(Intent(this, AddProductActivity::class.java))
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_home
        
        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> true
                R.id.nav_recipes -> {
                    startActivity(Intent(this, RecipesActivity::class.java))
                    false
                }
                R.id.nav_biostats -> {
                    startActivity(Intent(this, BiostatsActivity::class.java))
                    false
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    false
                }
                else -> false
            }
        }

        loadWeeklyInsight()
        
        val sharedPref = getSharedPreferences("UserStats", MODE_PRIVATE)
        if (sharedPref.getString("saved_weight", "").isNullOrEmpty()) {
            auraNotifications.scheduleMetricReminder()
        }
    }

    private fun loadWeeklyInsight() {
        val tvSummary = findViewById<TextView>(R.id.tvWeeklySummary)
        val tvRating = findViewById<TextView>(R.id.tvQLRating)
        
        val sharedPref = getSharedPreferences("UserStats", MODE_PRIVATE)
        val weight = sharedPref.getString("saved_weight", "not set")
        val height = sharedPref.getString("saved_height", "not set")
        val age = sharedPref.getString("user_age", "not set")
        
        val config = generationConfig {
            temperature = 0.7f
        }

        val safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
        )

        val generativeModel = GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = apiKey,
            generationConfig = config,
            safetySettings = safetySettings
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prompt = "You are Aura. Analyze these user stats: Weight=$weight, Height=$height, Age=$age. Provide a 2-sentence weekly wellness summary and a Quality of Life (Q.L) rating out of 10. format: [Summary] [Rating]"
                val response = generativeModel.generateContent(prompt)
                val resultText = response.text ?: ""
                
                withContext(Dispatchers.Main) {
                    if (resultText.contains("Rating")) {
                        val rating = resultText.substringAfterLast("Rating").trim().removePrefix(":").trim()
                        tvRating.text = "Q.L: $rating"
                        tvSummary.text = resultText.substringBeforeLast("Rating").trim()
                    } else {
                        tvSummary.text = resultText
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvSummary.text = "Aura Error: ${e.message}"
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.nav_home

        val settingsPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val bgColor = settingsPref.getInt("theme_bg", Color.BLACK)
        findViewById<View>(android.R.id.content).setBackgroundColor(bgColor)

        val imgThumb = findViewById<ImageView>(R.id.imgProfileThumb)
        val sharedPref = getSharedPreferences("UserStats", MODE_PRIVATE)
        val savedUriString = sharedPref.getString("profile_uri", null)

        if (savedUriString != null) {
            try {
                val uri = Uri.parse(savedUriString)
                imgThumb.setImageURI(uri)
                imgThumb.imageTintList = null
            } catch (e: Exception) {
                imgThumb.setImageResource(android.R.drawable.ic_menu_camera)
            }
        }
    }
}
