package com.example.wellnessapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnessapp.databinding.ActivityPaymentBinding

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Receive data from the Intent
        val name = intent.getStringExtra("PRODUCT_NAME") ?: "Wellness Product"
        val price = intent.getStringExtra("PRODUCT_PRICE") ?: "0.00"

        // Set text using binding
        binding.txtProductName.text = name
        binding.txtProductCost.text = "Kes $price"

        binding.btnSubmitPayment.setOnClickListener {
            val phone = binding.etPaymentPhone.text.toString().trim()
            if (phone.length < 10) {
                binding.etPaymentPhone.error = "Invalid number"
            } else {
                Toast.makeText(this, "Initiating M-Pesa for $name", Toast.LENGTH_LONG).show()
            }
        }
    }
}
