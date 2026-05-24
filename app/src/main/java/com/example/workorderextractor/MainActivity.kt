package com.example.workorderextractor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.workorderextractor.data.AppDatabase
import com.example.workorderextractor.data.WorkOrder
import com.example.workorderextractor.ui.theme.WorkOrderExtractorTheme
import com.example.workorderextractor.utils.WorkOrderExtractor
import com.example.workorderextractor.viewmodel.WorkOrderViewModel
import com.example.workorderextractor.viewmodel.WorkOrderViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getInstance(this)
        setContent {
            WorkOrderExtractorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(db)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(db: AppDatabase) {
    val navController = rememberNavController()
    val factory = WorkOrderViewModelFactory(db)
    val viewModel: WorkOrderViewModel = viewModel(factory = factory)

    NavHost(navController = navController, startDestination = "input") {
        composable("input") {
            InputScreen(viewModel, onNavigateToList = { navController.navigate("list") })
        }
        composable("list") {
            ListScreen(viewModel, onBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun InputScreen(viewModel: WorkOrderViewModel, onNavigateToList: () -> Unit) {
    var rawText by remember { mutableStateOf("") }
    var saveMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Paste Work Order Article", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = rawText,
            onValueChange = { rawText = it },
            modifier = Modifier.fillMaxWidth().height(250.dp),
            placeholder = { Text("Paste the entire work order text here...") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(onClick = {
                if (rawText.isNotBlank()) {
                    val extracted = WorkOrderExtractor.extract(rawText)
                    if (extracted.jobId.isNotBlank()) {
                        viewModel.insertOrder(extracted)
                        saveMessage = "Saved successfully! (Job ID: ${extracted.jobId})"
                        rawText = ""
                    } else {
                        saveMessage = "Failed to extract data. Check format."
                    }
                } else {
                    saveMessage = "Please paste some text."
                }
            }) {
                Text("Extract & Save")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onNavigateToList) {
                Text("Show All")
            }
        }
        if (saveMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(saveMessage, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ListScreen(viewModel: WorkOrderViewModel, onBack: () -> Unit) {
    val orders by viewModel.orders.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Text("<-")
            }
            Text("All Saved Work Orders", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(orders) { order ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Job ID: ${order.jobId}", style = MaterialTheme.typography.titleMedium)
                        Text("Grid: ${order.grid}")
                        Text("Service No: ${order.serviceNumber}")
                        Text("Address: ${order.addressA}")
                        Text("Date: ${order.appointmentDate} Time: ${order.appointmentTime}")
                        Text("Contact: ${order.contactName}")
                        Text("Status: ${order.status}")
                        Text("PID Desc: ${order.pidDesc}")
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}
