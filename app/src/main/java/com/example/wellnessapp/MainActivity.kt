package com.example.wellnessapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Glimmer Home: The entry point for Dyllan's Medical Evolution.
 * Optimized to prevent Main Thread blocking during AI reasoning.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var auraNotifications: AuraNotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        applyTheme()

        auraNotifications = AuraNotificationManager(this)
        
        setupUI()
        observeAuraStatus()

        // 8GB RAM OPTIMIZATION: Wait 1 second before starting the heavy 3.6GB map
        // to let the UI finish its first frame and prevent launch-lag.
        lifecycleScope.launch {
            delay(1000)
            GlimmerEngine.warmup(this@MainActivity)
            loadWeeklyInsight()
        }
        
        val sharedPref = getSharedPreferences("UserStats", MODE_PRIVATE)
        if (sharedPref.getString("saved_weight", "").isNullOrEmpty()) {
            auraNotifications.scheduleMetricReminder()
        }
    }

    private fun setupUI() {
        findViewById<CardView>(R.id.profileCircle).setOnClickListener {
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
    }

    private fun observeAuraStatus() {
        val tvSummary = findViewById<TextView>(R.id.tvWeeklySummary)
        
        lifecycleScope.launch {
            GlimmerEngine.loadingStatus.collectLatest { status ->
                when (status) {
                    GlimmerEngine.Status.LOADING -> {
                        tvSummary.text = "AURA Core: Waking up in background..."
                    }
                    GlimmerEngine.Status.READY -> {
                        tvSummary.text = "AURA Core: Online. All systems fluid."
                        loadWeeklyInsight()
                    }
                    GlimmerEngine.Status.ERROR_MISSING_MODEL -> {
                        tvSummary.text = "AURA Error: gemma.litertlm missing."
                    }
                    GlimmerEngine.Status.ERROR_INIT_FAILED -> {
                        tvSummary.text = "AURA Error: Initialization Failed."
                    }
                    else -> {}
                }
            }
        }
    }

    private fun applyTheme() {
        val settingsPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val bgColor = settingsPref.getInt("theme_bg", Color.parseColor("#0A0E21"))
        val accentColor = settingsPref.getInt("theme_accent", Color.parseColor("#4DEEE1"))
        
        val rootLayout = findViewById<View>(R.id.mainRootLayout)
        rootLayout?.setBackgroundColor(bgColor)
        
        findViewById<TextView>(R.id.tvAppTitle)?.setTextColor(accentColor)
        
        val cardAlpha = Color.argb(40, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor))
        
        val cards = listOf(
            R.id.cardWeeklySummary, R.id.cardBiostats, R.id.cardNutrition, 
            R.id.cardRecipes, R.id.cardRoutines
        )
        
        cards.forEach { id ->
            findViewById<CardView>(id)?.setCardBackgroundColor(cardAlpha)
        }

        findViewById<BottomNavigationView>(R.id.bottomNavigation)?.setBackgroundColor(bgColor)
    }

    private fun loadWeeklyInsight() {
        val tvSummary = findViewById<TextView>(R.id.tvWeeklySummary)
        val tvRating = findViewById<TextView>(R.id.tvQLRating)
        
        val sharedPref = getSharedPreferences("UserStats", MODE_PRIVATE)
        val weight = sharedPref.getString("saved_weight", "not set")
        val height = sharedPref.getString("saved_height", "not set")
        val age = sharedPref.getString("user_age", "not set")

        lifecycleScope.launch {
            val engine = GlimmerEngine.getInstance(this@MainActivity) ?: return@launch

            try {
                val prompt = "Context: Dyllan, Medical Student Path. Stats: W=$weight, H=$height, A=$age. Task: 2-sentence wellness summary. Format: [Summary] [Rating: X/10]"
                
                // CRITICAL 8GB FIX: Run inference on Dispatchers.IO to prevent Main Thread lag
                val result = withContext(Dispatchers.IO) {
                    engine.generateResponse(prompt)
                } ?: ""
                
                if (result.contains("Rating")) {
                    val rating = result.substringAfterLast("Rating").trim().removePrefix(":").trim()
                    tvRating.text = "Q.L: $rating"
                    tvSummary.text = result.substringBeforeLast("Rating").trim()
                } else {
                    tvSummary.text = result
                }
            } catch (e: Exception) {
                // Silent catch to prevent UI disruption
            }
        }
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
        findViewById<BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.nav_home

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
