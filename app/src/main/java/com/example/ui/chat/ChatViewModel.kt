package com.example.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Content
import com.example.data.GenerateContentRequest
import com.example.data.Part
import com.example.data.RetrofitClient
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Message(val text: String, val isUser: Boolean)

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    fun sendMessage(text: String) {
        val userMessage = Message(text, true)
        _messages.value = _messages.value + userMessage
        
        viewModelScope.launch {
            val request = GenerateContentRequest(
                contents = _messages.value.map { msg ->
                    Content(parts = listOf(Part(text = msg.text)))
                }
            )
            
            try {
                val response = RetrofitClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
                val botMessage = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response"
                _messages.value = _messages.value + Message(botMessage, false)
            } catch (e: Exception) {
                _messages.value = _messages.value + Message("Error: ${e.message}", false)
            }
        }
    }
}
