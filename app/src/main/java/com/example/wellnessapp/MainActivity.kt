package com.example.wellnessapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Profile Circle (Top Right)
        val profileCircle = findViewById<CardView>(R.id.profileCircle)
        profileCircle.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // 2. Main Dashboard Buttons
        findViewById<Button>(R.id.btnBiostats).setOnClickListener {
            startActivity(Intent(this, BiostatsActivity::class.java))
        }

        findViewById<Button>(R.id.btnNutrition).setOnClickListener {
            startActivity(Intent(this, MainActivity2::class.java))
        }

        findViewById<Button>(R.id.btnRecipes).setOnClickListener {
            startActivity(Intent(this, RecipesActivity::class.java))
        }

        findViewById<Button>(R.id.btnPersonalized).setOnClickListener {
            startActivity(Intent(this, PersonalizedActivity::class.java))
        }

        // 3. iOS Bottom Navigation Handling
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_home
        
        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> true // Current
                R.id.nav_recipes -> {
                    startActivity(Intent(this, RecipesActivity::class.java))
                    false // Don't highlight if we're moving to a full activity without fragment
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

    override fun onResume() {
        super.onResume()
        
        // Ensure Home is selected when returning to dashboard
        findViewById<BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.nav_home

        // Update profile photo thumb
        val imgThumb = findViewById<ImageView>(R.id.imgProfileThumb)
        val sharedPref = getSharedPreferences("UserStats", MODE_PRIVATE)
        val savedUriString = sharedPref.getString("profile_uri", null)

        if (savedUriString != null) {
            try {
                imgThumb.setImageURI(savedUriString.toUri())
            } catch (e: Exception) {
                imgThumb.setImageResource(android.R.drawable.ic_menu_camera)
            }
        }
    }
}
