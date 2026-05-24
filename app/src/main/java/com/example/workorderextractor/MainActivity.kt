package com.example.workorderextractor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.workorderextractor.ui.theme.WorkOrderExtractorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WorkOrderExtractorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MinimalScreen()
                }
            }
        }
    }
}

@Composable
fun MinimalScreen() {
    var text by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("Press button to test") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Work Order Extractor",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Input test") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                result = if (text.isNotEmpty()) {
                    "Input received: ${text.take(20)}..."
                } else {
                    "Please enter something"
                }
            }
        ) {
            Text("Test Button")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = result,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
