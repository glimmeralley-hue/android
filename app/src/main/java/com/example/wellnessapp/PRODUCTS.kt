package com.example.wellnessapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class PRODUCTSActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var productList: ArrayList<ProductModel>
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        val recyclerView = findViewById<RecyclerView>(R.id.rvProducts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        productList = ArrayList()
        adapter = ProductAdapter(productList) { clickedItem ->
            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra("PRODUCT_NAME", clickedItem.name)
            intent.putExtra("PRODUCT_PRICE", clickedItem.price)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        dbRef = FirebaseDatabase.getInstance().getReference("Products")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (data in snapshot.children) {
                    val item = data.getValue(ProductModel::class.java)
                    item?.let { productList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PRODUCTSActivity, "Database Error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
