package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class AssistantAudioState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

class VoiceManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsReady = false

    private val _audioState = MutableStateFlow(AssistantAudioState.IDLE)
    val audioState: StateFlow<AssistantAudioState> = _audioState.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    var onSpeechRecognized: ((String) -> Unit)? = null

    init {
        try {
            textToSpeech = TextToSpeech(context, this)
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error initializing TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            textToSpeech?.let { tts ->
                val locale = Locale("hi", "IN")
                val result = tts.setLanguage(locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.language = Locale.US
                }
                tts.setPitch(1.05f)
                tts.setSpeechRate(1.0f)
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _audioState.value = AssistantAudioState.SPEAKING
                    }

                    override fun onDone(utteranceId: String?) {
                        _audioState.value = AssistantAudioState.IDLE
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _audioState.value = AssistantAudioState.IDLE
                    }
                })
            }
        }
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w("VoiceManager", "Speech recognition not available on device")
            return
        }

        stopSpeaking()
        stopListening()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _audioState.value = AssistantAudioState.LISTENING
                        _recognizedText.value = ""
                    }

                    override fun onBeginningOfSpeech() {
                        _audioState.value = AssistantAudioState.LISTENING
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _audioAmplitude.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _audioState.value = AssistantAudioState.THINKING
                    }

                    override fun onError(error: Int) {
                        _audioState.value = AssistantAudioState.IDLE
                        _audioAmplitude.value = 0f
                    }

                    override fun onResults(results: Bundle?) {
                        _audioAmplitude.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull().orEmpty()
                        if (text.isNotBlank()) {
                            _recognizedText.value = text
                            _audioState.value = AssistantAudioState.THINKING
                            onSpeechRecognized?.invoke(text)
                        } else {
                            _audioState.value = AssistantAudioState.IDLE
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.firstOrNull()?.let {
                            _recognizedText.value = it
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e("VoiceManager", "Failed to start listening", e)
            _audioState.value = AssistantAudioState.IDLE
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error stopping recognizer", e)
        }
        if (_audioState.value == AssistantAudioState.LISTENING) {
            _audioState.value = AssistantAudioState.IDLE
        }
        _audioAmplitude.value = 0f
    }

    fun speak(text: String) {
        if (!isTtsReady || textToSpeech == null) return
        stopListening()
        // Strip out markdown formatting symbols for speech
        val cleanedText = text
            .replace(Regex("```[\\s\\S]*?```"), "Code generated in dashboard")
            .replace(Regex("[#*`_~]"), "")
            .trim()

        if (cleanedText.isBlank()) return

        _audioState.value = AssistantAudioState.SPEAKING
        textToSpeech?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, "LAPRRI_SPEECH_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() {
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
        }
        if (_audioState.value == AssistantAudioState.SPEAKING) {
            _audioState.value = AssistantAudioState.IDLE
        }
    }

    fun setSpeechRate(rate: Float) {
        textToSpeech?.setSpeechRate(rate)
    }

    fun setPitch(pitch: Float) {
        textToSpeech?.setPitch(pitch)
    }

    fun destroy() {
        stopSpeaking()
        stopListening()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
