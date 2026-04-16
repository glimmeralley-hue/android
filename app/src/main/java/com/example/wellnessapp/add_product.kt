package com.example.wellnessapp

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddProductActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private var imageUri: Uri? = null

    // UI Elements
    private lateinit var ivProductPhoto: ImageView
    private lateinit var etProductName: EditText
    private lateinit var etProductPrice: EditText
    private lateinit var etProductDesc: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnSaveProduct: Button

    // 1. Image Picker Logic
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            ivProductPhoto.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        // 2. Initialize Firebase Reference (Points to 'Products' folder in your Glimmer RTDB)
        database = FirebaseDatabase.getInstance().getReference("Products")

        // 3. Bind UI Components - EXACT MATCH to XML IDs
        ivProductPhoto = findViewById(R.id.ivProductPhoto)
        etProductName = findViewById(R.id.etProductName)
        etProductPrice = findViewById(R.id.etProductPrice)
        etProductDesc = findViewById(R.id.etProductDesc)
        progressBar = findViewById(R.id.progressBar)
        btnSaveProduct = findViewById(R.id.btnSaveProduct)
        val btnSelectPhoto = findViewById<Button>(R.id.btnSelectPhoto)

        // 4. Set Click Listeners
        btnSelectPhoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        btnSaveProduct.setOnClickListener {
            uploadProductData()
        }
    }

    private fun uploadProductData() {
        val name = etProductName.text.toString().trim()
        val price = etProductPrice.text.toString().trim()
        val desc = etProductDesc.text.toString().trim()

        // Validation
        if (name.isEmpty() || price.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading state (ProgressBar appears)
        progressBar.visibility = View.VISIBLE
        btnSaveProduct.isEnabled = false

        // 5. Create a Unique ID (The "Node" name in your JSON tree)
        val productId = database.push().key ?: return

        // 6. Create Product Object
        val productMap = hashMapOf(
            "productId" to productId,
            "name" to name,
            "price" to "Kes $price",
            "description" to desc,
            "imageUrl" to "https://placeholder.com/product.jpg" // Placeholder for now
        )

        // 7. Push to the URL: https://glimmer-dad0e-default-rtdb.firebaseio.com/Products
        database.child(productId).setValue(productMap)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show()
                finish() // Go back to the previous screen
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                btnSaveProduct.isEnabled = true
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
