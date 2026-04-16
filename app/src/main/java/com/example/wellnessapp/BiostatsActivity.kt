package com.example.wellnessapp

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnessapp.R.id.tvBMIResult

class BiostatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biostats)

        val weightInput = findViewById<EditText>(R.id.editWeight)
        val heightInput = findViewById<EditText>(R.id.editHeight)
        val tvBMIResult = findViewById<TextView>(tvBMIResult)
        val saveBtn = findViewById<Button>(R.id.btnSaveStats)
        val backBtn = findViewById<Button>(R.id.btnHomeBiostats)

        val sharedPref = getSharedPreferences("UserStats", Context.MODE_PRIVATE)

        // Load existing data
        weightInput.setText(sharedPref.getString("saved_weight", ""))
        heightInput.setText(sharedPref.getString("saved_height", ""))

        saveBtn.setOnClickListener {
            val weight = weightInput.text.toString()
            val height = heightInput.text.toString()

            if (weight.isNotEmpty() && height.isNotEmpty()) {
                // Save to SharedPreferences
                with(sharedPref.edit()) {
                    putString("saved_weight", weight)
                    putString("saved_height", height)
                    apply()
                }

                // Calculate BMI locally
                val w = weight.toFloat()
                val h = height.toFloat() / 100
                val bmi = w / (h * h)
                tvBMIResult.text = String.format("BMI: %.1f", bmi)

                Toast.makeText(this, "Metrics Synced", Toast.LENGTH_SHORT).show()
            }
        }

        backBtn.setOnClickListener { finish() }
    }
}
