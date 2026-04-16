package com.example.wellnessapp

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class BiostatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biostats)

        val weightInput = findViewById<EditText>(R.id.editWeight)
        val heightInput = findViewById<EditText>(R.id.editHeight)
        val tvBMIResult = findViewById<TextView>(R.id.tvBMIResult)
        val saveBtn = findViewById<Button>(R.id.btnSaveStats)
        val backBtn = findViewById<Button>(R.id.btnHomeBiostats)

        val sharedPref = getSharedPreferences("UserStats", Context.MODE_PRIVATE)
        val auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance().getReference("Biostats")

        // Load existing data from SharedPreferences
        weightInput.setText(sharedPref.getString("saved_weight", ""))
        heightInput.setText(sharedPref.getString("saved_height", ""))

        saveBtn.setOnClickListener {
            val weight = weightInput.text.toString()
            val height = heightInput.text.toString()

            if (weight.isNotEmpty() && height.isNotEmpty()) {
                val w = weight.toFloat()
                val h = height.toFloat() / 100
                val bmi = w / (h * h)
                tvBMIResult.text = String.format("BMI: %.1f", bmi)

                // 1. Save to SharedPreferences (Local Cache)
                with(sharedPref.edit()) {
                    putString("saved_weight", weight)
                    putString("saved_height", height)
                    apply()
                }

                // 2. Save to Firebase Database (Cloud Table)
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    val stats = Biostats(
                        weight = weight,
                        height = height,
                        bmi = bmi,
                        timestamp = System.currentTimeMillis()
                    )
                    database.child(uid).setValue(stats).addOnSuccessListener {
                        Toast.makeText(this, "Cloud Sync Successful", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Cloud Sync Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                Toast.makeText(this, "Metrics Saved", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        backBtn.setOnClickListener { finish() }
    }
}
