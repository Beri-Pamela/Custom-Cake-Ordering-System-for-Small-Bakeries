package com.example.thelmapam_project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(val text: String, val isUser: Boolean)

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {

    // Using a free API key here is NOT recommended for production, but this handles demo logic.
    // In a real production app, use an API key sourced securely from BuildConfig or backend.
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY ?: "DEFAULT_KEY" 
    )

    private val chat = generativeModel.startChat(
        history = listOf(
            content(role = "user") { text("You are an expert cake designer and assistant for SweetCreations Cake Designer. Be friendly, warm, and suggest artisanal cake ideas!") },
            content(role = "model") { text("Absolutely! I'm here to help you design the perfect artisanal cake. What are you looking for?") }
        )
    )

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hi there! I'm your SweetCreations Assistant. How can I help you design your cake today?", false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        _messages.value = _messages.value + ChatMessage(userMessage, true)
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = chat.sendMessage(userMessage)
                response.text?.let { reply ->
                    _messages.value = _messages.value + ChatMessage(reply, false)
                }
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage("Sorry, I encountered an error. Please try again.", false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
