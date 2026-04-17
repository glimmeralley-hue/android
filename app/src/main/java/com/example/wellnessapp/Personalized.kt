package com.example.wellnessapp

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * AURA Reasoning Core: Specialized in Medical Study & Flashcards.
 * Features: Voice-to-Text, Voice Activation, and Full App Autonomy.
 * Optimized for Redmi Note 12 Pro (8GB RAM / 2B Lite Core).
 */
class PersonalizedActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var auraVoice: AuraVoice
    private lateinit var database: AppDatabase
    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var auraPulse: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personalized)

        ThemeManager.applyTheme(this)
        auraVoice = AuraVoice(this)
        database = AppDatabase.getDatabase(this)
        auraPulse = findViewById(R.id.auraPulseRoutine)

        val rvChat = findViewById<RecyclerView>(R.id.rvChatRoutine)
        val etInput = findViewById<EditText>(R.id.etMessageInputRoutine)
        val btnSend = findViewById<CardView>(R.id.btnSendRoutine)
        val btnMic = findViewById<CardView>(R.id.btnMicRoutine)
        
        chatAdapter = ChatAdapter(messages)
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = chatAdapter

        setupSpeechRecognizer()
        observeMessages()
        observeAuraStatus(btnSend)

        btnSend.setOnClickListener {
            processMessage(etInput.text.toString())
            etInput.text.clear()
        }

        btnMic.setOnClickListener {
            startListening()
        }

        // Voice Activation: Trigger if intent comes from background listener
        if (intent.getBooleanExtra("VOICE_ACTIVATED", false)) {
            startListening()
        }
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                startAuraPulse()
                Toast.makeText(this@PersonalizedActivity, "AURA Listening...", Toast.LENGTH_SHORT).show()
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { stopAuraPulse() }
            override fun onError(error: Int) { stopAuraPulse() }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    processMessage(matches[0])
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        }
        speechRecognizer?.startListening(intent)
    }

    private fun processMessage(text: String) {
        if (text.trim().isEmpty()) return
        
        saveMessageToDb(text, true)
        val loadingOverlay = findViewById<View>(R.id.loadingOverlay)
        loadingOverlay.visibility = View.VISIBLE
        startAuraPulse()

        lifecycleScope.launch {
            // AURA AUTONOMY PROTOCOL: 
            // Analyze prompt for app commands (e.g., "Open Stats", "New Weight")
            handleAutonomyCommands(text.lowercase())

            val result = GlimmerEngine.generate(this@PersonalizedActivity, text)
            
            loadingOverlay.visibility = View.GONE
            stopAuraPulse()

            result?.let { 
                saveMessageToDb(it, false)
                auraVoice.speak(it)
            }
        }
    }

    /**
     * AURA AUTONOMY: Allows the AI to navigate or perform system actions.
     */
    private fun handleAutonomyCommands(input: String) {
        when {
            input.contains("open biostats") || input.contains("show stats") -> {
                startActivity(Intent(this, BiostatsActivity::class.java))
            }
            input.contains("recipes") -> {
                startActivity(Intent(this, RecipesActivity::class.java))
            }
            input.contains("nutrition") -> {
                startActivity(Intent(this, MainActivity2::class.java))
            }
            input.contains("go home") -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }

    private fun startAuraPulse() {
        auraPulse.visibility = View.VISIBLE
        val anim = AlphaAnimation(0.3f, 1.0f).apply {
            duration = 600
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }
        auraPulse.startAnimation(anim)
    }

    private fun stopAuraPulse() {
        auraPulse.clearAnimation()
        auraPulse.visibility = View.INVISIBLE
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
            database.chatDao().getMessages("aura_chat").collectLatest { entities ->
                messages.clear()
                entities.forEach { entity ->
                    messages.add(ChatMessage(entity.text, entity.isUser))
                }
                chatAdapter.notifyDataSetChanged()
                findViewById<RecyclerView>(R.id.rvChatRoutine).scrollToPosition(messages.size - 1)
                
                if (messages.isEmpty()) {
                    val welcomeMsg = "AURA Core online. System status: Autonomous & Voice Enabled. Ready to architect your medical evolution."
                    saveMessageToDb(welcomeMsg, false)
                    auraVoice.speak(welcomeMsg)
                }
            }
        }
    }

    private fun saveMessageToDb(text: String, isUser: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            database.chatDao().insert(ChatMessageEntity(text = text, isUser = isUser, chatKey = "aura_chat"))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        auraVoice.shutDown()
        speechRecognizer?.destroy()
    }
}
