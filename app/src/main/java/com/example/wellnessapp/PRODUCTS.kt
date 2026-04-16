package com.example.wellnessapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ActivityProducts : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var productList: ArrayList<PRODUCTS>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        val listView = findViewById<ListView>(R.id.lvProducts)
        productList = ArrayList()

        // This MUST match the "Products" node we used to save data
        dbRef = FirebaseDatabase.getInstance().getReference("Products")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                val displayList = ArrayList<String>()

                for (data in snapshot.children) {
                    val item = data.getValue(PRODUCTS::class.java)
                    item?.let {
                        productList.add(it)
                        // Using .name and .price because those are the IDs we saved!
                        displayList.add("${it.name} — Kes ${it.price}")
                    }
                }

                val adapter = ArrayAdapter(this@ActivityProducts, android.R.layout.simple_list_item_1, displayList)
                listView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        listView.setOnItemClickListener { _, _, position, _ ->
            val clickedItem = productList[position]
            val intent = Intent(this, PaymentActivity::class.java)

            // Sending the data to your Payment Page
            intent.putExtra("PRODUCT_NAME", clickedItem.name)
            intent.putExtra("PRODUCT_PRICE", clickedItem.price)
            startActivity(intent)
        }
    }
}
