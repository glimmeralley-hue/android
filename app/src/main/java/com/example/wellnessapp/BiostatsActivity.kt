package com.example.wellnessapp

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BiostatsActivity : AppCompatActivity() {
    
    private val apiKey = "AIzaSyDV2Fu1LHO7JaAMSPx-CdYzUpTwpzAqNjU"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biostats)

        val weightInput = findViewById<EditText>(R.id.editWeight)
        val heightInput = findViewById<EditText>(R.id.editHeight)
        val tvBMIResult = findViewById<TextView>(R.id.tvBMIResult)
        val tvAIBriefing = findViewById<TextView>(R.id.tvAIBriefing)
        val saveBtn = findViewById<Button>(R.id.btnSaveStats)
        val backBtn = findViewById<Button>(R.id.btnHomeBiostats)

        val sharedPref = getSharedPreferences("UserStats", Context.MODE_PRIVATE)
        val auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance().getReference("Biostats")

        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash-latest",
            apiKey = apiKey
        )

        // Load existing data
        val savedWeight = sharedPref.getString("saved_weight", "")
        val savedHeight = sharedPref.getString("saved_height", "")
        weightInput.setText(savedWeight)
        heightInput.setText(savedHeight)
        
        if (savedWeight != null && savedHeight != null && savedWeight.isNotEmpty() && savedHeight.isNotEmpty()) {
            generateAIBrief(savedWeight, savedHeight, tvAIBriefing, generativeModel)
        }

        saveBtn.setOnClickListener {
            val weight = weightInput.text.toString()
            val height = heightInput.text.toString()

            if (weight.isNotEmpty() && height.isNotEmpty()) {
                val w = weight.toFloat()
                val h = height.toFloat() / 100
                val bmi = w / (h * h)
                tvBMIResult.text = String.format("BMI: %.1f", bmi)

                // Save Locally
                with(sharedPref.edit()) {
                    putString("saved_weight", weight)
                    putString("saved_height", height)
                    apply()
                }

                // AI Analysis
                generateAIBrief(weight, height, tvAIBriefing, generativeModel)

                // Save to Firebase
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    val stats = Biostats(
                        weight = weight,
                        height = height,
                        bmi = bmi,
                        timestamp = System.currentTimeMillis()
                    )
                    database.child(uid).setValue(stats)
                }
                Toast.makeText(this, "Metrics Updated", Toast.LENGTH_SHORT).show()
            }
        }

        backBtn.setOnClickListener { finish() }
    }

    private fun generateAIBrief(weight: String, height: String, tvBrief: TextView, model: GenerativeModel) {
        tvBrief.text = "AI is analyzing your metrics..."
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prompt = "Based on a weight of $weight kg and height of $height cm, provide a very brief, professional wellness insight. Include BMI category and one actionable health tip. Keep it under 3 sentences."
                val response = model.generateContent(prompt)
                withContext(Dispatchers.Main) {
                    tvBrief.text = response.text ?: "Analysis unavailable."
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvBrief.text = "AI Analysis paused: ${e.message}"
                }
            }
        }
    }
}
