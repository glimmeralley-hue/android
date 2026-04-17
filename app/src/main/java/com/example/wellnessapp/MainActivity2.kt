package com.example.wellnessapp

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Glimmer Nutritionist: Specialized Reasoning for Dyllan's Metabolic Health.
 * Optimized for 8GB RAM with Async Loading (Singleton).
 */
class MainActivity2 : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nutrition)

        ThemeManager.applyTheme(this)
        database = AppDatabase.getDatabase(this)

        val rvChat = findViewById<RecyclerView>(R.id.rvChat)
        val etInput = findViewById<EditText>(R.id.etMessageInput)
        val btnSend = findViewById<CardView>(R.id.btnSend)
        val loadingOverlay = findViewById<View>(R.id.loadingOverlayNutrition)

        chatAdapter = ChatAdapter(messages)
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = chatAdapter
        
        observeMessages()
        observeAuraStatus(btnSend)

        btnSend.setOnClickListener {
            val userText = etInput.text.toString().trim()
            if (userText.isNotEmpty()) {
                etInput.text.clear()
                saveMessageToDb(userText, true)

                loadingOverlay?.visibility = View.VISIBLE

                lifecycleScope.launch {
                    val engine = GlimmerEngine.getInstance(this@MainActivity2)
                    if (engine == null) {
                        loadingOverlay?.visibility = View.GONE
                        saveMessageToDb("AURA Nutrition: Engine not ready. Check gemma.litertlm.", false)
                        return@launch
                    }

                    try {
                        val systemContext = "Persona: Glimmer Nutritionist. Expert in metabolic health for medical professionals. Path: Kenya to Brazil. Task: Analyze nutrition data locally. Response Tone: Analytical, supportive, plain text."
                        val prompt = "$systemContext\nUser: $userText\nNutritionist:"
                        val result = withContext(Dispatchers.IO) { engine.generateResponse(prompt) }
                        
                        loadingOverlay?.visibility = View.GONE
                        result?.let { saveMessageToDb(it, false) }
                    } catch (e: Exception) {
                        loadingOverlay?.visibility = View.GONE
                        saveMessageToDb("Nutrition Engine Error: ${e.message}", false)
                    }
                }
            }
        }
    }

    private fun observeAuraStatus(sendButton: CardView) {
        lifecycleScope.launch {
            GlimmerEngine.loadingStatus.collectLatest { status ->
                sendButton.isEnabled = (status == GlimmerEngine.Status.READY)
                sendButton.alpha = if (sendButton.isEnabled) 1.0f else 0.5f
            }
        }
    }

    private fun observeMessages() {
        lifecycleScope.launch {
            database.chatDao().getMessages("nutrition_chat").collectLatest { entities ->
                messages.clear()
                entities.forEach { entity ->
                    messages.add(ChatMessage(entity.text, entity.isUser))
                }
                chatAdapter.notifyDataSetChanged()
                findViewById<RecyclerView>(R.id.rvChat).scrollToPosition(messages.size - 1)
                
                if (messages.isEmpty()) {
                    saveMessageToDb("Hello Dyllan. I am Glimmer Nutritionist. Ready to architect your metabolic diet plans for your medical path.", false)
                }
            }
        }
    }

    private fun saveMessageToDb(text: String, isUser: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.chatDao().insert(ChatMessageEntity(text = text, isUser = isUser, chatKey = "nutrition_chat"))
        }
    }
}
