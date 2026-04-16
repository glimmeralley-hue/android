package com.example.wellnessapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            imgProfile.setImageURI(it)
            imgProfile.imageTintList = null // Remove tint when image is loaded
            
            try {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                // Fallback for non-persistable URIs
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val editAge = findViewById<EditText>(R.id.editAge)
        val editGoal = findViewById<EditText>(R.id.editGoal)
        val editMedical = findViewById<EditText>(R.id.editMedicalHistory)
        val btnSave = findViewById<Button>(R.id.btnSaveProfile)
        val btnSettings = findViewById<CardView>(R.id.btnSettings)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val imageContainer = findViewById<CardView>(R.id.profileImageContainer)
        imgProfile = findViewById(R.id.imgProfileFull)

        val sharedPref = getSharedPreferences("UserStats", Context.MODE_PRIVATE)

        editAge.setText(sharedPref.getString("user_age", ""))
        editGoal.setText(sharedPref.getString("user_goal", ""))
        editMedical.setText(sharedPref.getString("user_medical", ""))

        val savedUriString = sharedPref.getString("profile_uri", null)
        if (savedUriString != null) {
            try {
                val uri = Uri.parse(savedUriString)
                imgProfile.setImageURI(uri)
                imgProfile.imageTintList = null
            } catch (e: Exception) {
                imgProfile.setImageResource(android.R.drawable.ic_menu_camera)
            }
        }

        imageContainer.setOnClickListener {
            pickImage.launch("image/*")
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnLogout.setOnClickListener {
            // Logic for "Sever Connection"
            val authPref = getSharedPreferences("Auth", Context.MODE_PRIVATE)
            authPref.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        btnSave.setOnClickListener {
            val age = editAge.text.toString()
            val goal = editGoal.text.toString()
            val medical = editMedical.text.toString()

            with(sharedPref.edit()) {
                putString("user_age", age)
                putString("user_goal", goal)
                putString("user_medical", medical)
                selectedImageUri?.let { putString("profile_uri", it.toString()) }
                apply()
            }

            Toast.makeText(this, "Identity Synchronized", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
