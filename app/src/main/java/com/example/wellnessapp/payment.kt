package com.example.wellnessapp

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.wellnessapp.databinding.ActivityPaymentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    
    // DARJA SANDBOX KEYS
    private val CONSUMER_KEY = "FTp5DGzxQW2p5MTNH0GChZA0kGhWsZ0GEICggAnIzK7aCV7K"
    private val CONSUMER_SECRET = "aZwNvU90vjtrGj9SkBwCGr8GQnl6gSxQT2HC76s65GMPKTXp5RKuRRyblGAUGWWl"
    private val BUSINESS_SHORT_CODE = "174379"
    private val PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"
    private val CALLBACK_URL = "https://mydomain.com/path"

    private lateinit var mpesaService: MpesaService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ThemeManager.applyTheme(this)

        val name = intent.getStringExtra("PRODUCT_NAME") ?: "Wellness Product"
        val price = intent.getStringExtra("PRODUCT_PRICE") ?: "1"
        val imageSource = intent.getStringExtra("PRODUCT_IMAGE")

        binding.txtProductName.text = name
        binding.txtProductCost.text = "Kes $price"

        if (imageSource != null) {
            if (!imageSource.startsWith("http")) {
                try {
                    val imageBytes = Base64.decode(imageSource, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    binding.imgProduct.setImageBitmap(decodedImage)
                } catch (e: Exception) {
                    binding.imgProduct.setImageResource(android.R.drawable.ic_menu_report_image)
                }
            } else {
                binding.imgProduct.load(imageSource) {
                    crossfade(true)
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_menu_report_image)
                }
            }
        }

        // Initialize Retrofit with Logging
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://sandbox.safaricom.co.ke/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        mpesaService = retrofit.create(MpesaService::class.java)

        binding.btnSubmitPayment.setOnClickListener {
            val phone = binding.etPaymentPhone.text.toString().trim()
            if (phone.isEmpty()) {
                binding.etPaymentPhone.error = "Phone number required"
                return@setOnClickListener
            }
            
            var formattedPhone = phone
            if (formattedPhone.startsWith("0")) {
                formattedPhone = "254" + formattedPhone.substring(1)
            } else if (formattedPhone.startsWith("+")) {
                formattedPhone = formattedPhone.substring(1)
            }

            getAccessToken { token ->
                if (token != null) {
                    performSTKPush(token, formattedPhone, price.toInt(), name)
                } else {
                    Toast.makeText(this, "Aura: Secure Token Negotiation Failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getAccessToken(callback: (String?) -> Unit) {
        val keys = "$CONSUMER_KEY:$CONSUMER_SECRET"
        val auth = "Basic " + Base64.encodeToString(keys.toByteArray(), Base64.NO_WRAP)

        mpesaService.getAccessToken(auth).enqueue(object : Callback<AccessToken> {
            override fun onResponse(call: Call<AccessToken>, response: Response<AccessToken>) {
                if (response.isSuccessful) {
                    callback(response.body()?.accessToken)
                } else {
                    Log.e("MpesaError", "Token Error: ${response.code()} ${response.errorBody()?.string()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<AccessToken>, t: Throwable) {
                Log.e("MpesaError", "Network Failure: ${t.message}")
                callback(null)
            }
        })
    }

    private fun performSTKPush(token: String, phone: String, amount: Int, productName: String) {
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val password = Base64.encodeToString(
            (BUSINESS_SHORT_CODE + PASSKEY + timestamp).toByteArray(),
            Base64.NO_WRAP
        )

        val request = STKPushRequest(
            businessShortCode = BUSINESS_SHORT_CODE,
            password = password,
            timestamp = timestamp,
            transactionType = "CustomerPayBillOnline",
            amount = amount,
            partyA = phone,
            partyB = BUSINESS_SHORT_CODE,
            phoneNumber = phone,
            callBackURL = CALLBACK_URL,
            accountReference = "WellnessApp",
            transactionDesc = "Payment for $productName"
        )

        mpesaService.sendSTKPush("Bearer $token", request).enqueue(object : Callback<STKPushResponse> {
            override fun onResponse(call: Call<STKPushResponse>, response: Response<STKPushResponse>) {
                if (response.isSuccessful) {
                    val checkoutRequestId = response.body()?.checkoutRequestID ?: ""
                    saveTransactionToFirebase(checkoutRequestId, productName, amount.toString(), phone)
                    Toast.makeText(this@PaymentActivity, "STK Push Sent. Check your phone.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@PaymentActivity, "STK Push Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<STKPushResponse>, t: Throwable) {
                Toast.makeText(this@PaymentActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveTransactionToFirebase(transactionId: String, productName: String, amount: String, phone: String) {
        val auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance().getReference("Transactions")
        val userId = auth.currentUser?.uid ?: "anonymous"

        val transaction = TransactionRecord(
            transactionId = transactionId,
            userId = userId,
            productName = productName,
            amount = amount,
            phoneNumber = phone,
            status = "Pending",
            timestamp = System.currentTimeMillis()
        )

        database.child(transactionId).setValue(transaction)
    }
}
