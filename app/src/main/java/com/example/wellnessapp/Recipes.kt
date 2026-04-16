package com.example.wellnessapp

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipesActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private val apiKey = "AIzaSyDV2Fu1LHO7JaAMSPx-CdYzUpTwpzAqNjU"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipes)

        val rvChat = findViewById<RecyclerView>(R.id.rvChatRecipes)
        val etInput = findViewById<EditText>(R.id.etMessageInputRecipes)
        val btnSend = findViewById<CardView>(R.id.btnSendRecipes)

        chatAdapter = ChatAdapter(messages)
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = chatAdapter

        val config = generationConfig {
            temperature = 0.8f
        }

        val safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
        )

        val generativeModel = GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = apiKey,
            generationConfig = config,
            safetySettings = safetySettings
        )

        addMessage("Greetings! I am your personal Chef AI (v2.0). Tell me what ingredients you have, or ask for a specific healthy recipe.", false)

        btnSend.setOnClickListener {
            val userText = etInput.text.toString().trim()
            if (userText.isNotEmpty()) {
                etInput.text.clear()
                addMessage(userText, true)

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = generativeModel.generateContent("You are a professional chef specializing in healthy, wellness-focused meals. Provide a detailed recipe or cooking advice for: $userText")
                        withContext(Dispatchers.Main) {
                            response.text?.let { addMessage(it, false) }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            addMessage("Chef error: ${e.message}", false)
                        }
                    }
                }
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messages.add(ChatMessage(text, isUser))
        chatAdapter.notifyItemInserted(messages.size - 1)
        findViewById<RecyclerView>(R.id.rvChatRecipes).scrollToPosition(messages.size - 1)
    }
}
