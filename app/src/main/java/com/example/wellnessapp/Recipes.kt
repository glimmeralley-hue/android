package com.example.wellnessapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class RecipesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipes)

        val btnHome = findViewById<Button>(R.id.btnHomeRecipes)
        btnHome.setOnClickListener {
            finish() // Slides the screen away and returns home
        }
    }
}
