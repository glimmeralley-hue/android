package com.example.wellnessapp

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * StudyActivity: AURA Medical Hub.
 * Features: Pomodoro Focus Timer, AI Flashcard Generation, and Study Architect Chat.
 */
class StudyActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var auraPulse: View
    private lateinit var tvTimerClock: TextView
    private var timer: CountDownTimer? = null
    private var isTimerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        ThemeManager.applyTheme(this)
        auraPulse = findViewById(R.id.auraPulseStudy)
        tvTimerClock = findViewById(R.id.tvTimerClock)

        val rvChat = findViewById<RecyclerView>(R.id.rvStudyChat)
        val etInput = findViewById<EditText>(R.id.etStudyInput)
        val btnSend = findViewById<CardView>(R.id.btnSendStudy)
        val btnFlashcards = findViewById<Button>(R.id.btnGenerateFlashcards)
        val btnStartTimer = findViewById<CardView>(R.id.btnStartTimer)

        chatAdapter = ChatAdapter(messages)
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = chatAdapter

        observeAuraStatus()

        btnSend.setOnClickListener {
            val text = etInput.text.toString()
            if (text.isNotEmpty()) {
                askStudyArchitect(text)
                etInput.text.clear()
            }
        }

        btnFlashcards.setOnClickListener {
            generateMedicalFlashcards()
        }

        btnStartTimer.setOnClickListener {
            toggleFocusTimer()
        }
    }

    private fun askStudyArchitect(query: String) {
        messages.add(ChatMessage(query, true))
        chatAdapter.notifyItemInserted(messages.size - 1)
        startAuraPulse()

        lifecycleScope.launch {
            val prompt = "Context: Medical Student Study Hub. Task: Answer medical query or explain concept. Query: $query"
            val result = GlimmerEngine.generate(this@StudyActivity, prompt)
            
            stopAuraPulse()
            result?.let {
                messages.add(ChatMessage(it, false))
                chatAdapter.notifyItemInserted(messages.size - 1)
                findViewById<RecyclerView>(R.id.rvStudyChat).scrollToPosition(messages.size - 1)
            }
        }
    }

    private fun generateMedicalFlashcards() {
        startAuraPulse()
        findViewById<TextView>(R.id.tvFlashcardStatus).text = "AURA: Constructing Active Recall Deck..."
        
        lifecycleScope.launch {
            val prompt = "Task: Generate 3 high-yield medical flashcards for anatomy or physiology. Format: [Q] [A]"
            val result = GlimmerEngine.generate(this@StudyActivity, prompt)
            
            stopAuraPulse()
            findViewById<TextView>(R.id.tvFlashcardStatus).text = result ?: "Failed to generate. Check RAM."
        }
    }

    private fun toggleFocusTimer() {
        if (isTimerRunning) {
            timer?.cancel()
            tvTimerClock.text = "25:00"
            isTimerRunning = false
        } else {
            isTimerRunning = true
            timer = object : CountDownTimer(1500000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val minutes = millisUntilFinished / 1000 / 60
                    val seconds = (millisUntilFinished / 1000) % 60
                    tvTimerClock.text = String.format("%02d:%02d", minutes, seconds)
                }
                override fun onFinish() {
                    tvTimerClock.text = "BREAK"
                    isTimerRunning = false
                }
            }.start()
        }
    }

    private fun observeAuraStatus() {
        lifecycleScope.launch {
            GlimmerEngine.loadingStatus.collectLatest { status ->
                if (status == GlimmerEngine.Status.READY && messages.isEmpty()) {
                    messages.add(ChatMessage("Medical Hub Online. Ready for Active Recall and Study Architecture.", false))
                    chatAdapter.notifyDataSetChanged()
                }
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
}
