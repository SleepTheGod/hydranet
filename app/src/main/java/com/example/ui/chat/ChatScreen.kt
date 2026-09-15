package com.example.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Prototype Caution Notice
        Text(
            text = "PROTOTYPE CAUTION: This feature uses a direct REST API for prototyping and is not for production use.",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { message ->
                Text(
                    text = "${if (message.isUser) "You" else "Gemini"}: ${message.text}",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        
        Row {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                viewModel.sendMessage(inputText)
                inputText = ""
            }) {
                Text("Send")
            }
        }
    }
}
