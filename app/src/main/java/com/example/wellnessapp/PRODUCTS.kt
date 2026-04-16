package com.example.wellnessapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class PRODUCTSActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var productList: ArrayList<ProductModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        val listView = findViewById<ListView>(R.id.lvProducts)
        productList = ArrayList()

        dbRef = FirebaseDatabase.getInstance().getReference("Products")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                val displayList = ArrayList<String>()

                for (data in snapshot.children) {
                    // Use ProductModel from Models.kt
                    val item = data.getValue(ProductModel::class.java)

                    item?.let {
                        productList.add(it)
                        displayList.add("${it.name} — Kes ${it.price}")
                    }
                }

                val adapter = ArrayAdapter(this@PRODUCTSActivity, android.R.layout.simple_list_item_1, displayList)
                listView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PRODUCTSActivity, "Database Error", Toast.LENGTH_SHORT).show()
            }
        })

        listView.setOnItemClickListener { _, _, position, _ ->
            val clickedItem = productList[position]
            val intent = Intent(this, PaymentActivity::class.java)
            // It picks up the data here and sends it to PaymentActivity
            intent.putExtra("PRODUCT_NAME", clickedItem.name)
            intent.putExtra("PRODUCT_PRICE", clickedItem.price)
            startActivity(intent)
        }
    }
}
