package com.example.wellnessapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*

class AddProductActivity : AppCompatActivity() {

    private var imageUri: Uri? = null
    private lateinit var ivProductImage: ImageView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) pickImage.launch("image/*")
        else Toast.makeText(this, "Aura requires storage access to proceed.", Toast.LENGTH_SHORT).show()
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            ivProductImage.setImageURI(it)
            ivProductImage.imageTintList = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        val etProductName = findViewById<EditText>(R.id.etProductName)
        val etProductPrice = findViewById<EditText>(R.id.etProductPrice)
        val etProductDescription = findViewById<EditText>(R.id.etProductDescription)
        val btnSaveProduct = findViewById<Button>(R.id.btnSaveProduct)
        ivProductImage = findViewById(R.id.ivProductImage)

        ivProductImage.setOnClickListener { checkPermissionAndPickImage() }

        btnSaveProduct.setOnClickListener {
            val name = etProductName.text.toString().trim()
            val price = etProductPrice.text.toString().trim()
            val description = etProductDescription.text.toString().trim()

            if (name.isEmpty() || price.isEmpty() || description.isEmpty() || imageUri == null) {
                Toast.makeText(this, "Incomplete Architecture: Details missing.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Convert to Base64 and save
            val base64Image = encodeImageToBase64(imageUri!!)
            if (base64Image != null) {
                saveProductToDatabase(name, price, description, base64Image)
            } else {
                Toast.makeText(this, "Aura: Image Processing Failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissionAndPickImage() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 
            Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            pickImage.launch("image/*")
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun encodeImageToBase64(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            
            // Scale down to prevent database bloat
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true)
            
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }

    private fun saveProductToDatabase(name: String, price: String, description: String, base64Image: String) {
        val database = FirebaseDatabase.getInstance().getReference("Products")
        val productId = database.push().key ?: UUID.randomUUID().toString()

        val productData = ProductModel(productId, name, price, description, base64Image)

        database.child(productId).setValue(productData).addOnSuccessListener {
            Toast.makeText(this, "System Update: Product Synchronized", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
