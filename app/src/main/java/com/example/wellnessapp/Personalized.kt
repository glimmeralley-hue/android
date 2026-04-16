package com.example.wellnessapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnessapp.R.id.btnBackRoutine

class PersonalizedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personalized)

        val btnBack = findViewById<Button>(btnBackRoutine)

        btnBack.setOnClickListener {
            finish()
        }
    }
}
