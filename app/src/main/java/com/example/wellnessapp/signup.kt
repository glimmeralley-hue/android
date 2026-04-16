package com.example.wellnessapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // 1. Initialize Firebase Auth and Realtime Database
        auth = FirebaseAuth.getInstance()
        // This points to the root of your URL: https://glimmer-dad0e-default-rtdb.firebaseio.com/
        database = FirebaseDatabase.getInstance().getReference("Users")

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val tvLoginRedirect = findViewById<TextView>(R.id.tvLoginRedirect)

        btnSignUp.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val weight = etWeight.text.toString().trim()

            // Simple Validation
            if (email.isEmpty() || password.length < 8 || name.isEmpty()) {
                Toast.makeText(this, "Check fields (Password must be 8+ chars)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Create User in Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid

                        // 3. Prepare the data "Table" entries
                        val userProfile = hashMapOf(
                            "uid" to uid,
                            "name" to name,
                            "email" to email,
                            "weight" to weight,
                            "created_at" to System.currentTimeMillis().toString()
                        )

                        // 4. Push data to the Realtime Database URL
                        if (uid != null) {
                            database.child(uid).setValue(userProfile)
                                .addOnSuccessListener {
                                    // Success! Data is now visible in your browser screenshot
                                    Toast.makeText(this, "Welcome to Glimmer, $name", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Database Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Auth Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        tvLoginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
