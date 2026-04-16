package com.example.wellnessapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class AddProductActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        val etProductName = findViewById<EditText>(R.id.etProductName)
        val etProductPrice = findViewById<EditText>(R.id.etProductPrice)
        val btnSaveProduct = findViewById<Button>(R.id.btnSaveProduct)

        val database = FirebaseDatabase.getInstance().getReference("Products")

        btnSaveProduct.setOnClickListener {
            val name = etProductName.text.toString().trim()
            val price = etProductPrice.text.toString().trim()

            if (name.isEmpty() || price.isEmpty()) {
                Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show()
            } else {
                val productId = database.push().key ?: ""

                // Using ProductModel from Models.kt
                val productData = ProductModel(
                    id = productId,
                    name = name,
                    price = price
                )

                database.child(productId).setValue(productData).addOnSuccessListener {
                    Toast.makeText(this, "Successfully Saved to Database", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, PaymentActivity::class.java)
                    intent.putExtra("PRODUCT_NAME", name)
                    intent.putExtra("PRODUCT_PRICE", price)
                    startActivity(intent)

                }.addOnFailureListener {
                    Toast.makeText(this, "Database Error: ${it.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
