package com.example.data

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val codeSnippet: String? = null
)

data class WebProject(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val htmlContent: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class QuickAction(
    val title: String,
    val prompt: String,
    val iconName: String
)

enum class AppNavigationTab {
    VOICE_HUB,
    CHAT,
    WEBFORGE,
    TOOLS,
    LICENSE_STATUS
}
