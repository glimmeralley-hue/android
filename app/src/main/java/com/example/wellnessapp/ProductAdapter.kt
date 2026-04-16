package com.example.wellnessapp

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class ProductAdapter(
    private val productList: List<ProductModel>,
    private val onItemClick: (ProductModel) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProductImage: ImageView = view.findViewById(R.id.ivProductItemImage)
        val tvProductName: TextView = view.findViewById(R.id.tvProductItemName)
        val tvProductPrice: TextView = view.findViewById(R.id.tvProductItemPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.tvProductName.text = product.name ?: "Unknown Product"
        holder.tvProductPrice.text = "Kes ${product.price ?: "0"}"
        
        val imageSource = product.imageUrl
        if (imageSource != null && !imageSource.startsWith("http")) {
            // Assume it's Base64
            try {
                val imageBytes = Base64.decode(imageSource, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.ivProductImage.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                holder.ivProductImage.setImageResource(android.R.drawable.ic_menu_report_image)
            }
        } else {
            // It's a URL or null
            holder.ivProductImage.load(imageSource) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_report_image)
            }
        }

        holder.itemView.setOnClickListener { onItemClick(product) }
    }

    override fun getItemCount(): Int = productList.size
}
