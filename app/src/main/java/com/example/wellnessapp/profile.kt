package com.example.wellnessapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class ProfileActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private var selectedImageUri: Uri? = null

    // 1. Initialize the Image Picker
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            imgProfile.setImageURI(it)
            // Persist permission for the URI so it loads after app restart
            contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val editAge = findViewById<EditText>(R.id.editAge)
        val editGoal = findViewById<EditText>(R.id.editGoal)
        val editMedical = findViewById<EditText>(R.id.editMedicalHistory)
        val btnSave = findViewById<Button>(R.id.btnSaveProfile)
        val imageContainer = findViewById<CardView>(R.id.profileImageContainer)
        imgProfile = findViewById(R.id.imgProfileFull)

        val sharedPref = getSharedPreferences("UserStats", Context.MODE_PRIVATE)

        // 2. Load existing data on Startup
        editAge.setText(sharedPref.getString("user_age", ""))
        editGoal.setText(sharedPref.getString("user_goal", ""))
        editMedical.setText(sharedPref.getString("user_medical", ""))

        val savedUriString = sharedPref.getString("profile_uri", null)
        if (savedUriString != null) {
            imgProfile.setImageURI(Uri.parse(savedUriString))
        }

        // 3. Trigger Gallery
        imageContainer.setOnClickListener {
            pickImage.launch("image/*")
        }

        // 4. Save All Data
        btnSave.setOnClickListener {
            val age = editAge.text.toString()
            val goal = editGoal.text.toString()
            val medical = editMedical.text.toString()

            with(sharedPref.edit()) {
                putString("user_age", age)
                putString("user_goal", goal)
                putString("user_medical", medical)
                // Save image URI if one was picked
                selectedImageUri?.let { putString("profile_uri", it.toString()) }
                apply()
            }

            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
            finish() // Return to Dashboard
        }
    }
}
