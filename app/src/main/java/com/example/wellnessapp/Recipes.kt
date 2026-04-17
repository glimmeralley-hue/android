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
 * Glimmer Chef: Specialized reasoning for Dyllan's wellness meals.
 * Optimized for 8GB RAM with Async Loading (Singleton).
 */
class RecipesActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipes)

        ThemeManager.applyTheme(this)
        database = AppDatabase.getDatabase(this)

        val rvChat = findViewById<RecyclerView>(R.id.rvChatRecipes)
        val etInput = findViewById<EditText>(R.id.etMessageInputRecipes)
        val btnSend = findViewById<CardView>(R.id.btnSendRecipes)
        val loadingOverlay = findViewById<View>(R.id.loadingOverlayRecipes) 

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
                    val engine = GlimmerEngine.getInstance(this@RecipesActivity)
                    if (engine == null) {
                        loadingOverlay?.visibility = View.GONE
                        saveMessageToDb("AURA Chef: Engine not ready. Check gemma.litertlm.", false)
                        return@launch
                    }

                    try {
                        val systemContext = "Persona: Glimmer Chef. Specialized in healthy, high-performance wellness meals. Journey: Kenya to Brazil Medical Path. Response Tone: Analytical and precise. Respond in plain text."
                        val prompt = "$systemContext\nUser: $userText\nChef:"
                        val result = withContext(Dispatchers.IO) { engine.generateResponse(prompt) }
                        
                        loadingOverlay?.visibility = View.GONE
                        result?.let { saveMessageToDb(it, false) }
                    } catch (e: Exception) {
                        loadingOverlay?.visibility = View.GONE
                        saveMessageToDb("Chef Local Error: ${e.message}", false)
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
            database.chatDao().getMessages("recipes_chat").collectLatest { entities ->
                messages.clear()
                entities.forEach { entity ->
                    messages.add(ChatMessage(entity.text, entity.isUser))
                }
                chatAdapter.notifyDataSetChanged()
                findViewById<RecyclerView>(R.id.rvChatRecipes).scrollToPosition(messages.size - 1)
                
                if (messages.isEmpty()) {
                    saveMessageToDb("Greetings! I am Glimmer Chef (v 4.0 Local). Tell me what ingredients you have, or ask for a specific healthy recipe aligned with your medical journey.", false)
                }
            }
        }
    }

    private fun saveMessageToDb(text: String, isUser: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.chatDao().insert(ChatMessageEntity(text = text, isUser = isUser, chatKey = "recipes_chat"))
        }
    }
}
