package com.example.wellnessapp

import android.content.Context
import android.os.Process
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * GlimmerEngine: Optimized for Gemma 2B (AI Edge) on 8GB Redmi hardware.
 * Includes diagnostic logging to break the "no response" silence.
 */
object GlimmerEngine {
    private const val TAG = "AURA_ARCHITECT"
    private var instance: LlmInference? = null
    private val initMutex = Mutex()
    private val inferenceMutex = Mutex()
    
    private val engineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    enum class Status { IDLE, LOADING, READY, ERROR_MISSING_MODEL, ERROR_INIT_FAILED }
    
    private val _loadingStatus = MutableStateFlow(Status.IDLE)
    val loadingStatus: StateFlow<Status> = _loadingStatus.asStateFlow()

    fun warmup(context: Context) {
        if (instance != null || _loadingStatus.value == Status.LOADING) return
        engineScope.launch {
            getInstance(context.applicationContext)
        }
    }

    suspend fun getInstance(context: Context): LlmInference? {
        if (instance != null) return instance

        return initMutex.withLock {
            if (instance != null) return@withLock instance

            _loadingStatus.value = Status.LOADING
            
            // Check for AI Edge extensions
            val possibleFiles = listOf("gemma.bin", "gemma.litertlm", "model.bin")
            var modelFile: File? = null
            
            for (name in possibleFiles) {
                val f = File(context.filesDir, name)
                if (f.exists()) {
                    modelFile = f
                    break
                }
            }
            
            if (modelFile == null) {
                Log.e(TAG, "FATAL: No model file found in ${context.filesDir}. Found files: ${context.filesDir.list()?.joinToString()}")
                _loadingStatus.value = Status.ERROR_MISSING_MODEL
                return@withLock null
            }

            try {
                Log.d(TAG, "AURA: Attempting to map ${modelFile.name} (${modelFile.length() / 1024 / 1024} MB)")
                System.gc() 
                
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(512) 
                    .setMaxTopK(1)    
                    .setPreferredBackend(LlmInference.Backend.CPU)
                    .build()

                instance = LlmInference.createFromOptions(context, options)
                _loadingStatus.value = Status.READY
                Log.d(TAG, "AURA: Core initialized successfully.")
                instance
            } catch (e: Exception) {
                Log.e(TAG, "AURA: Initialization Failed: ${e.message}")
                e.printStackTrace()
                _loadingStatus.value = Status.ERROR_INIT_FAILED
                null
            }
        }
    }

    suspend fun generate(context: Context, prompt: String): String? = withContext(Dispatchers.IO) {
        Log.d(TAG, "AURA: Request received. Waiting for lock...")
        val currentInstance = getInstance(context) ?: return@withContext null
        
        inferenceMutex.withLock {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            return@withContext try {
                // Formatting for Gemma 2 (Standard from AI Edge)
                val formattedPrompt = "<start_of_turn>user\n$prompt<end_of_turn>\n<start_of_turn>model\n"
                
                Log.d(TAG, "AURA: Model is processing prompt...")
                val startTime = System.currentTimeMillis()
                val result = currentInstance.generateResponse(formattedPrompt)
                val duration = System.currentTimeMillis() - startTime
                
                Log.d(TAG, "AURA: Response generated in ${duration}ms: $result")
                result.trim()
            } catch (e: Exception) {
                Log.e(TAG, "AURA: Inference error: ${e.message}")
                null
            }
        }
    }
}
