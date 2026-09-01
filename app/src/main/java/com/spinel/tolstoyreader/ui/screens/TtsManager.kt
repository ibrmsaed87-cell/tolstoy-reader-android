package com.spinel.tolstoyreader.ui.screens

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class TtsManager(context: Context, private val onInitResult: (Boolean) -> Unit) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                onInitResult(true)
            } else {
                onInitResult(false)
            }
        }
    }

    fun setLanguage(languageCode: String): Boolean {
        if (!isInitialized) return false
        val locale = Locale(languageCode)
        val result = tts?.setLanguage(locale)
        return result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    fun speak(text: String) {
        if (!isInitialized) return
        
        // Chunk the text to prevent TTS from failing on very long texts.
        // Android TTS has a limit of TextToSpeech.getMaxSpeechInputLength() which is usually 4000
        val maxLength = TextToSpeech.getMaxSpeechInputLength() - 100
        
        val chunks = text.chunked(maxLength)
        for ((index, chunk) in chunks.withIndex()) {
            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            tts?.speak(chunk, queueMode, null, "TTS_CHUNK_$index")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
