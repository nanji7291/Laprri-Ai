package com.example.voice

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Base64
import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class VoiceManager(
    private val apiKey: String,
    private val onStateChange: (Boolean) -> Unit
) {
    private var webSocket: WebSocket? = null
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var isStreaming = false

    private val sampleRate = 16000
    private val channelConfigIn = AudioFormat.CHANNEL_IN_MONO
    private val channelConfigOut = AudioFormat.CHANNEL_OUT_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfigIn, audioFormat)

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    fun startLiveSession() {
        val url = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent?key=$apiKey"
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("GeminiLive", "WebSocket Connected")
                sendInitialSetup(webSocket)
                startAudioRecording(webSocket)
                initAudioTrack()
                isStreaming = true
                onStateChange(true)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleIncomingAudio(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("GeminiLive", "Connection failed: ${t.message}")
                stopLiveSession()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("GeminiLive", "Session Closed")
                stopLiveSession()
            }
        })
    }

    private fun sendInitialSetup(webSocket: WebSocket) {
        val setupJson = JSONObject().apply {
            put("setup", JSONObject().apply {
                put("model", "models/gemini-2.0-flash-exp")
                put("generationConfig", JSONObject().apply {
                    put("responseModalities", arrayOf("AUDIO"))
                    put("speechConfig", JSONObject().apply {
                        put("voiceConfig", JSONObject().apply {
                            put("prebuiltVoiceConfig", JSONObject().apply {
                                put("voiceName", "Puck")
                            })
                        })
                    })
                })
            })
        }
        webSocket.send(setupJson.toString())
    }

    @SuppressLint("MissingPermission")
    private fun startAudioRecording(webSocket: WebSocket) {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfigIn,
            audioFormat,
            bufferSize
        )

        audioRecord?.startRecording()

        Thread {
            val audioBuffer = ByteArray(bufferSize)
            while (isStreaming) {
                val readBytes = audioRecord?.read(audioBuffer, 0, bufferSize) ?: 0
                if (readBytes > 0) {
                    val base64Audio = Base64.encodeToString(audioBuffer, 0, readBytes, Base64.NO_WRAP)
                    val pcmChunk = JSONObject().apply {
                        put("realtimeInput", JSONObject().apply {
                            put("mediaChunks", arrayOf(JSONObject().apply {
                                put("mimeType", "audio/pcm")
                                put("data", base64Audio)
                            }))
                        })
                    }
                    webSocket.send(pcmChunk.toString())
                }
            }
        }.start()
    }

    private fun initAudioTrack() {
        val minTrackBuffer = AudioTrack.getMinBufferSize(sampleRate, channelConfigOut, audioFormat)
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(audioFormat)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfigOut)
                    .build()
            )
            .setBufferSizeInBytes(minTrackBuffer)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
    }

    private fun handleIncomingAudio(jsonString: String) {
        try {
            val json = JSONObject(jsonString)
            val parts = json.optJSONObject("serverContent")
                ?.optJSONObject("modelTurn")
                ?.optJSONArray("parts")

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    val inlineData = part.optJSONObject("inlineData")
                    if (inlineData != null) {
                        val base64Data = inlineData.getString("data")
                        val pcmBytes = Base64.decode(base64Data, Base64.DEFAULT)
                        audioTrack?.write(pcmBytes, 0, pcmBytes.size)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiLive", "Error parsing incoming audio", e)
        }
    }

    fun stopLiveSession() {
        isStreaming = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null

        webSocket?.close(1000, "User stopped session")
        webSocket = null

        onStateChange(false)
    }
}
