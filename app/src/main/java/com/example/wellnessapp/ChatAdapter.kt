package com.example.wellnessapp

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chatCard: CardView = view.findViewById(R.id.chatCard)
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        val messageContainer: LinearLayout = view.findViewById(R.id.messageContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.tvMessage.text = message.message

        val params = holder.chatCard.layoutParams as LinearLayout.LayoutParams
        if (message.isUser) {
            params.gravity = Gravity.END
            holder.chatCard.setCardBackgroundColor(0xFFFFFFFF.toInt())
            holder.tvMessage.setTextColor(0xFF000000.toInt())
        } else {
            params.gravity = Gravity.START
            holder.chatCard.setCardBackgroundColor(0x1AFFFFFF.toInt())
            holder.tvMessage.setTextColor(0xFFFFFFFF.toInt())
        }
        holder.chatCard.layoutParams = params
    }

    override fun getItemCount(): Int = messages.size
}
