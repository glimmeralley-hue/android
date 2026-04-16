package com.example.wellnessapp

import android.os.Bundle
import android.view.View
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

class PersonalizedActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private val apiKey = "AIzaSyDV2Fu1LHO7JaAMSPx-CdYzUpTwpzAqNjU"
    private lateinit var auraVoice: AuraVoice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personalized)

        // Apply Global Morphic Theme
        ThemeManager.applyTheme(this)

        auraVoice = AuraVoice(this)

        val rvChat = findViewById<RecyclerView>(R.id.rvChatRoutine)
        val etInput = findViewById<EditText>(R.id.etMessageInputRoutine)
        val btnSend = findViewById<CardView>(R.id.btnSendRoutine)
        val loadingOverlay = findViewById<View>(R.id.loadingOverlay)

        // Load Persistent Chat History
        val savedMessages = ChatPersistence.loadMessages(this, "aura_chat")
        messages.addAll(savedMessages)

        chatAdapter = ChatAdapter(messages)
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = chatAdapter

        val config = generationConfig {
            temperature = 0.9f
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

        val systemContext = """
            You are 'Aura', a highly personalized fitness architect (v 3.1). 
            Your personality is encouraging but analytical and slightly futuristic. 
            You remember the user's progress. 
            Always refer to yourself as Aura. Keep responses concise and impactful.
            Do not use markdown formatting like asterisks or hashes.
        """.trimIndent()

        if (messages.isEmpty()) {
            val welcomeMsg = "Aura (v 3.1) online. I'm ready to architect your evolution. What are we targeting today?"
            addMessage(welcomeMsg, false)
            etInput.postDelayed({ auraVoice.speak(welcomeMsg) }, 1000)
        } else {
            rvChat.scrollToPosition(messages.size - 1)
        }

        btnSend.setOnClickListener {
            val userText = etInput.text.toString().trim()
            if (userText.isNotEmpty()) {
                etInput.text.clear()
                addMessage(userText, true)
                
                loadingOverlay.visibility = View.VISIBLE

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = generativeModel.generateContent("$systemContext\nUser says: $userText")
                        withContext(Dispatchers.Main) {
                            loadingOverlay.visibility = View.GONE
                            response.text?.let { 
                                val cleanText = it.replace("*", "").replace("#", "")
                                addMessage(cleanText, false)
                                auraVoice.speak(cleanText)
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            loadingOverlay.visibility = View.GONE
                            addMessage("Aura Error: ${e.message}", false)
                        }
                    }
                }
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messages.add(ChatMessage(text, isUser))
        chatAdapter.notifyItemInserted(messages.size - 1)
        findViewById<RecyclerView>(R.id.rvChatRoutine).scrollToPosition(messages.size - 1)
        ChatPersistence.saveMessages(this, "aura_chat", messages)
    }

    override fun onDestroy() {
        super.onDestroy()
        auraVoice.shutDown()
    }
}
