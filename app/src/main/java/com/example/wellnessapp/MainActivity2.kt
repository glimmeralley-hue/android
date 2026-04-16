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

class MainActivity2 : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private val apiKey = "AIzaSyDV2Fu1LHO7JaAMSPx-CdYzUpTwpzAqNjU"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nutrition)

        // Apply Global Morphic Theme
        ThemeManager.applyTheme(this)

        val rvChat = findViewById<RecyclerView>(R.id.rvChat)
        val etInput = findViewById<EditText>(R.id.etMessageInput)
        val btnSend = findViewById<CardView>(R.id.btnSend)

        // Load Persistent Chat History
        val savedMessages = ChatPersistence.loadMessages(this, "nutrition_chat")
        messages.addAll(savedMessages)

        chatAdapter = ChatAdapter(messages)
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = chatAdapter
        
        if (messages.isEmpty()) {
            addMessage("Hello! I'm your Nutrition AI (v 3.1). Ask me anything about food, calories, or diet plans.", false)
        } else {
            rvChat.scrollToPosition(messages.size - 1)
        }

        val config = generationConfig {
            temperature = 0.7f
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

        btnSend.setOnClickListener {
            val userText = etInput.text.toString().trim()
            if (userText.isNotEmpty()) {
                etInput.text.clear()
                addMessage(userText, true)

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = generativeModel.generateContent("You are a professional nutritionist. Answer this: $userText")
                        withContext(Dispatchers.Main) {
                            response.text?.let { 
                                val cleanText = it.replace("*", "").replace("#", "")
                                addMessage(cleanText, false) 
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            addMessage("Error: ${e.message}", false)
                        }
                    }
                }
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messages.add(ChatMessage(text, isUser))
        chatAdapter.notifyItemInserted(messages.size - 1)
        findViewById<RecyclerView>(R.id.rvChat).scrollToPosition(messages.size - 1)
        
        // Save history every time a message is added
        ChatPersistence.saveMessages(this, "nutrition_chat", messages)
    }
}
