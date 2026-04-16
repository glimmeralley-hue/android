package com.example.wellnessapp

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnessapp.R.id.tvIngredientsList
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// 1. DATA MODELS
data class NinjaResponse(val items: List<NinjaFood>)
data class NinjaFood(
    val name: String,
    val calories: Double,
    val protein_g: Double,
    val sugar_g: Double
)

// 2. API INTERFACE (Hardened)
interface CalorieNinjaService {
    @GET("v1/nutrition")
    fun getNutrition(
        @Header("X-Api-Key") apiKey: String, // Ensure this matches exactly
        @Query("query") query: String
    ): Call<NinjaResponse>
}

class MainActivity2 : AppCompatActivity() {

    // Ensure NO spaces before or after the key
    private val NINJA_KEY = "HOH3uJGdnxpeJm9fGWU6sftzBoKVcYOT7B4QxvFZ"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nutrition)

        // UI Initialization
        val etFoodInput = findViewById<EditText>(R.id.etFoodInput)
        val btnAnalyze = findViewById<Button>(R.id.btnAnalyze)
        val tvLiveAnalysis = findViewById<TextView>(R.id.tvLiveAnalysis)
        val tvMedicalWarning = findViewById<TextView>(R.id.tvMedicalWarning)

        // Recipe Component Views
        val tvRecipeTitle = findViewById<TextView>(R.id.tvRecipeTitle)
        val tvIngredientsList = findViewById<TextView>(tvIngredientsList)
        val tvInstructions = findViewById<TextView>(R.id.tvInstructions)

        // Setup Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.calorieninjas.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(CalorieNinjaService::class.java)

        btnAnalyze.setOnClickListener {
            val userQuery = etFoodInput.text.toString().trim()

            if (userQuery.isNotEmpty()) {
                tvLiveAnalysis.text = "UPLINK: REQUESTING DATA..."

                service.getNutrition(NINJA_KEY, userQuery).enqueue(object : Callback<NinjaResponse> {
                    override fun onResponse(call: Call<NinjaResponse>, response: Response<NinjaResponse>) {
                        if (response.isSuccessful) {
                            val foods = response.body()?.items
                            if (!foods.isNullOrEmpty()) {
                                val totalKcal = foods.sumOf { it.calories }.toInt()
                                val totalSugar = foods.sumOf { it.sugar_g }

                                // Update Analysis Card
                                tvLiveAnalysis.text = "RESULT: $totalKcal KCAL"

                                // Integrated Recipe Component logic
                                updateRecipeComponent(userQuery, tvRecipeTitle, tvIngredientsList, tvInstructions)

                                // Medical Career Path logic (Sugar warning)
                                if (totalSugar > 25.0) {
                                    tvMedicalWarning.text = "ADVISORY: HIGH GLUCOSE IMPACT"
                                    tvMedicalWarning.setTextColor(Color.parseColor("#FF453A"))
                                } else {
                                    tvMedicalWarning.text = "CLINICAL INTEGRITY: OPTIMAL"
                                    tvMedicalWarning.setTextColor(Color.parseColor("#30D158"))
                                }
                            } else {
                                tvLiveAnalysis.text = "ERROR: DATA NOT FOUND"
                            }
                        } else {
                            // If this still says 400, your API Key has likely expired or is blocked
                            tvLiveAnalysis.text = "ERROR ${response.code()}: VERIFICATION FAILED"
                        }
                    }

                    override fun onFailure(call: Call<NinjaResponse>, t: Throwable) {
                        tvLiveAnalysis.text = "LINK FAILURE: NETWORK OFFLINE"
                    }
                })
            }
        }
    }

    // Logic to update the Recipe Component based on the input
    private fun updateRecipeComponent(query: String, title: TextView, ingredients: TextView, instructions: TextView) {
        when {
            query.contains("beef", true) || query.contains("nyama", true) -> {
                title.text = "STARK PROTEIN: LEAN BEEF"
                ingredients.text = "INGREDIENTS //\n• 200g Lean Beef\n• 1 tsp Black Pepper\n• Minimal Sea Salt"
                instructions.text = "1. Sear on high heat for 3 mins per side.\n2. Rest for 5 mins to retain nutrients.\n\nMedical: Optimal B12 and Zinc for active recovery."
            }
            query.contains("rice", true) -> {
                title.text = "CARB LOAD: BROWN RICE"
                ingredients.text = "INGREDIENTS //\n• 1 Cup Brown Rice\n• 2 Cups Water"
                instructions.text = "1. Rinse thoroughly to remove excess arsenic.\n2. Simmer for 40 mins.\n\nMedical: Low Glycemic Index for sustained energy."
            }
            else -> {
                title.text = "GENERIC MEAL DATA"
                ingredients.text = "INGREDIENTS //\nStandard components detected."
                instructions.text = "Consult clinical guidelines for specific preparation methods."
            }
        }
    }
}
