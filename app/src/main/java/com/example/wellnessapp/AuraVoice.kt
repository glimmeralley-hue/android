package com.example.wellnessapp

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

class AuraVoice(context: Context) : TextToSpeech.OnInitListener {

    private val tts = TextToSpeech(context, this)
    private var isReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isReady = true
                tts.setPitch(1.1f) // Slightly higher pitch for Aura
                tts.setSpeechRate(1.0f)
            }
        }
    }

    fun speak(text: String) {
        if (isReady) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun shutDown() {
        tts.stop()
        tts.shutdown()
    }
}
