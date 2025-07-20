package com.jovicheer.whisper_voice_wear_native.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.gms.wearable.*

class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val nodeClient by lazy { Wearable.getNodeClient(this) }
    private var uiState by mutableStateOf("—")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** 初始化 Wearable 通訊：這段超重要！ **/
        nodeClient.connectedNodes
            .addOnSuccessListener { nodes ->
                for (node in nodes) {
                    Log.d("WearTest", "Connected to: ${node.displayName} (${node.id})")
                }
            }
            .addOnFailureListener {
                Log.e("WearTest", "Failed to get nodes: ${it.message}")
            }

        setContent {
            MaterialTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = uiState)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { sendGetInput() }) {
                            Text("Get Input")
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        messageClient.addListener(this)
    }

    override fun onPause() {
        messageClient.removeListener(this)
        super.onPause()
    }

    private fun sendGetInput() {
        nodeClient.connectedNodes
            .addOnSuccessListener { nodes ->
                for (node in nodes) {
                    Log.d("WearTest", "Sending to: ${node.displayName}")
                    messageClient.sendMessage(node.id, "/get_input", null)
                        .addOnSuccessListener {
                            Log.d("WearTest", "Message sent to ${node.id}")
                        }
                        .addOnFailureListener {
                            Log.e("WearTest", "Send failed: ${it.message}")
                        }
                }
            }
            .addOnFailureListener {
                Log.e("WearTest", "Node fetch failed: ${it.message}")
            }
    }

    override fun onMessageReceived(event: MessageEvent) {
        if (event.path == "/input_response") {
            val text = String(event.data)
            Log.d("WearTest", "Received response: $text")
            uiState = text
        }
    }
}
