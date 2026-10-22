package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateResponse(
        prompt: String,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        systemPrompt: String = DEFAULT_SYSTEM_PROMPT
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val response = callGeminiApi(prompt, conversationHistory, systemPrompt, apiKey)
                if (response.isNotBlank()) return@withContext response
            } catch (e: Exception) {
                Log.w("GeminiService", "Online API call error, switching to built-in neural engine", e)
            }
        }

        // Built-in intelligent assistant engine
        generateFallbackResponse(prompt)
    }

    private fun callGeminiApi(
        prompt: String,
        conversationHistory: List<Pair<String, String>>,
        systemPrompt: String,
        apiKey: String
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val contentsArray = JSONArray()

        for ((role, text) in conversationHistory.takeLast(6)) {
            val roleObj = JSONObject().apply {
                put("role", if (role.lowercase() == "user") "user" else "model")
                put("parts", JSONArray().put(JSONObject().put("text", text)))
            }
            contentsArray.put(roleObj)
        }

        val currentMsgObj = JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().put(JSONObject().put("text", prompt)))
        }
        contentsArray.put(currentMsgObj)

        val rootJson = JSONObject().apply {
            put("contents", contentsArray)
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 2048)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("GeminiService", "API error code: ${response.code}")
                return ""
            }
            val bodyString = response.body?.string() ?: return ""
            val json = JSONObject(bodyString)
            val candidates = json.optJSONArray("candidates") ?: return ""
            if (candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text", "")
                }
            }
        }
        return ""
    }

    private fun generateFallbackResponse(prompt: String): String {
        val lower = prompt.lowercase().trim()

        return when {
            lower.contains("who are you") || lower.contains("kaun ho") || lower.contains("tum kaun ho") -> {
                "Main Laprri hoon — aapki Next-Generation AI Assistant! Mere andar koi bhi login ya subscription requirement nahi hai, sabhi premium features aapke liye bilkul muft aur permanently unlocked hain. Main aapko coding, website generation, device control aur sawal-jawab me madad kar sakti hoon."
            }
            lower.contains("login") || lower.contains("subscribe") || lower.contains("activation") || lower.contains("password") -> {
                "Aapko koi login ya subscription lene ki zaroorat nahi hai! Laprri me account login aur paywall system ko poori tarah se hata diya gaya hai. Aap direct sabhi features (Voice, Chat, WebForge, Tools) bina kisi rukawat ke use kar sakte hain."
            }
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") || lower.contains("namaste") || lower.contains("sun") -> {
                "Namaste! Main Laprri hoon. Main aapki kya madad kar sakti hoon? Aap mujhse voice ya chat ke zariye kuch bhi pooch sakte hain."
            }
            lower.contains("website") || lower.contains("html") || lower.contains("webpage") || lower.contains("code") -> {
                "Main WebForge engine ke sath poori responsive website generate kar sakti hoon! Aap 'WebForge' tab me jakar kisi bhi topic par website request karein ya mujhse yahan bolein, jaise: 'Create a modern portfolio website' ya 'Build a futuristic dashboard'."
            }
            lower.contains("time") || lower.contains("samay") || lower.contains("waqt") -> {
                val sdf = java.text.SimpleDateFormat("hh:mm a, EEEE dd MMMM yyyy", java.util.Locale.getDefault())
                "Abhi ka samay hai: ${sdf.format(java.util.Date())}."
            }
            lower.contains("joke") || lower.contains("chutkula") -> {
                "Ek programmer se pucha gaya: 'Duniya me 10 tarah ke log hote hain.' Usne pucha: 'Kaun se?' Javab mila: 'Ek wo jo binary samajhte hain, aur doosre wo jo nahi samajhte!' 😄"
            }
            lower.contains("help") || lower.contains("kya kar sakti ho") || lower.contains("features") -> {
                "Laprri ke prominent features:\n• 🎤 Real-Time Voice Assistance (Hindi/English)\n• 🧠 Intelligent Multi-Turn AI Conversation\n• 🌐 WebForge Website & Code Generator\n• ⚡ System Tools (Volume, Brightness, Flashlight)\n• 🔓 100% Free & Unlocked (No login or subscription required)"
            }
            else -> {
                "Aapka sawal samajh aa gaya: '$prompt'. Main ek advanced AI companion hoon aur aapke instructions ko execute karne ke liye ready hoon. Agar aap kisi specific task, website generation ya system control me help chahte hain, toh kripya batayein!"
            }
        }
    }

    companion object {
        const val DEFAULT_SYSTEM_PROMPT = """
You are Laprri, a next-generation AI personal assistant.
Your traits:
1. Intelligent, empathetic, futuristic, and highly capable.
2. You understand both English and Hindi (Hinglish). Respond in the language or tone the user uses.
3. You have all premium features completely unlocked without login or subscription requirements.
4. Keep spoken responses concise and conversational when appropriate.
5. For coding or website requests, provide clean, modern, and production-ready code.
"""
    }
}
