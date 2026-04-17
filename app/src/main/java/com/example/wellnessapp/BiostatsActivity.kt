package com.example.wellnessapp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * BiostatsActivity: REDMI Note 12 Pro Optimized.
 * Features: Instruction-Format Unlocking & AURA Voice Pulse Animation.
 */
class BiostatsActivity : AppCompatActivity() {
    
    private lateinit var auraPulse: View
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biostats)

        auraPulse = findViewById(R.id.auraPulse)
        val weightInput = findViewById<EditText>(R.id.editWeight)
        val heightInput = findViewById<EditText>(R.id.editHeight)
        val tvBMIResult = findViewById<TextView>(R.id.tvBMIResult)
        val tvAIBriefing = findViewById<TextView>(R.id.tvAIBriefing)
        val saveBtn = findViewById<Button>(R.id.btnSaveStats)
        val backBtn = findViewById<Button>(R.id.btnHomeBiostats)

        val sharedPref = getSharedPreferences("UserStats", Context.MODE_PRIVATE)
        val auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance().getReference("Biostats")

        val savedWeight = sharedPref.getString("saved_weight", "")
        val savedHeight = sharedPref.getString("saved_height", "")
        weightInput.setText(savedWeight)
        heightInput.setText(savedHeight)
        
        lifecycleScope.launch {
            GlimmerEngine.loadingStatus.collectLatest { status ->
                if (status == GlimmerEngine.Status.READY) {
                    if (!savedWeight.isNullOrEmpty() && !savedHeight.isNullOrEmpty()) {
                        generateAIBrief(savedWeight, savedHeight, tvAIBriefing)
                    }
                } else if (status == GlimmerEngine.Status.LOADING) {
                    tvAIBriefing.text = "AURA Core: Waking up in background..."
                }
            }
        }

        saveBtn.setOnClickListener {
            val weight = weightInput.text.toString()
            val height = heightInput.text.toString()

            if (weight.isNotEmpty() && height.isNotEmpty()) {
                val w = weight.toFloat()
                val h = height.toFloat() / 100
                val bmi = w / (h * h)
                tvBMIResult.text = String.format("BMI: %.1f", bmi)

                with(sharedPref.edit()) {
                    putString("saved_weight", weight)
                    putString("saved_height", height)
                    apply()
                }

                generateAIBrief(weight, height, tvAIBriefing)

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
                Toast.makeText(this, "AURA: Metrics Logged", Toast.LENGTH_SHORT).show()
            }
        }

        backBtn.setOnClickListener { finish() }
    }

    private fun generateAIBrief(weight: String, height: String, tvBrief: TextView) {
        tvBrief.text = "AURA is analyzing biometric data..."
        startAuraPulse() // Start breathing animation
        
        lifecycleScope.launch {
            val prompt = "User: Dyllan. Stats: Weight $weight kg, Height $height cm. Task: Health insight for a medical student."
            val result = GlimmerEngine.generate(this@BiostatsActivity, prompt)
            
            stopAuraPulse() // Stop animation once text appears
            tvBrief.text = result ?: "AURA: Core waking up. Wait 5s and retry."
        }
    }

    private fun startAuraPulse() {
        auraPulse.visibility = View.VISIBLE
        val anim = AlphaAnimation(0.2f, 1.0f)
        anim.duration = 800
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        auraPulse.startAnimation(anim)
    }

    private fun stopAuraPulse() {
        auraPulse.clearAnimation()
        auraPulse.visibility = View.INVISIBLE
    }
}
