package com.example.workorderextractor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

    NavHost(navController = navController, startDestination = "add") {
        composable("add") {
            AddWorkOrderScreen(viewModel, onNavigateToList = { navController.navigate("list") })
        }
        composable("list") {
            WorkOrderListScreen(viewModel, onEdit = { orderId ->
                navController.navigate("edit/$orderId")
            }, onBack = { navController.popBackStack() })
        }
        composable("edit/{orderId}") { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId")?.toIntOrNull() ?: 0
            EditWorkOrderScreen(viewModel, orderId, onBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun AddWorkOrderScreen(viewModel: WorkOrderViewModel, onNavigateToList: () -> Unit) {
    var rawText by remember { mutableStateOf("") }
    var extractedOrder by remember { mutableStateOf<WorkOrder?>(null) }
    var showPreview by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Enter work order text", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = rawText,
            onValueChange = { rawText = it },
            modifier = Modifier.fillMaxWidth().height(200.dp),
            placeholder = { Text("Paste or type the work order content here...") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (rawText.isNotBlank()) {
                extractedOrder = WorkOrderExtractor.extract(rawText)
                showPreview = true
            }
        }) {
            Text("Extract Data")
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (showPreview && extractedOrder != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Extraction Preview", style = MaterialTheme.typography.titleMedium)
                    Divider()
                    Text("Job ID: ${extractedOrder!!.jobId}")
                    Text("Service No: ${extractedOrder!!.serviceNumber}")
                    Text("A-End Address: ${extractedOrder!!.addressA}")
                    Text("B-End Address: ${extractedOrder!!.addressB}")
                    Text("Date: ${extractedOrder!!.appointmentDate}")
                    Text("Time: ${extractedOrder!!.appointmentTime}")
                    Text("Contact: ${extractedOrder!!.contactName}")
                    Text("Phone: ${extractedOrder!!.contactPhone}")
                    Text("Status: ${extractedOrder!!.status}")
                    Text("PID Desc: ${extractedOrder!!.pidDesc}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Button(onClick = {
                            viewModel.insertOrder(extractedOrder!!)
                            onNavigateToList()
                        }) {
                            Text("Save")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { showPreview = false }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onNavigateToList) {
            Text("View All Saved Orders")
        }
    }
}

@Composable
fun WorkOrderListScreen(viewModel: WorkOrderViewModel, onEdit: (Int) -> Unit, onBack: () -> Unit) {
    val orders by viewModel.orders.collectAsState()
    var searchText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Text("←")
            }
            Text("Saved Work Orders", style = MaterialTheme.typography.titleLarge)
        }
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                viewModel.updateSearchQuery(it)
            },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(orders) { order ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    onClick = { onEdit(order.id) }) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Job ID: ${order.jobId}", style = MaterialTheme.typography.titleMedium)
                        Text("Date: ${order.appointmentDate} ${order.appointmentTime}")
                        Text("Contact: ${order.contactName} (${order.contactPhone})")
                        Text("Address: ${order.addressA.take(20)}...")
                    }
                }
            }
        }
    }
}

@Composable
fun EditWorkOrderScreen(viewModel: WorkOrderViewModel, orderId: Int, onBack: () -> Unit) {
    var order by remember { mutableStateOf<WorkOrder?>(null) }
    LaunchedEffect(orderId) {
        order = viewModel.getOrderById(orderId)
    }

    if (order == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var jobId by remember { mutableStateOf(order!!.jobId) }
    var serviceNumber by remember { mutableStateOf(order!!.serviceNumber) }
    var addressA by remember { mutableStateOf(order!!.addressA) }
    var addressB by remember { mutableStateOf(order!!.addressB) }
    var appointmentDate by remember { mutableStateOf(order!!.appointmentDate) }
    var appointmentTime by remember { mutableStateOf(order!!.appointmentTime) }
    var contactName by remember { mutableStateOf(order!!.contactName) }
    var contactPhone by remember { mutableStateOf(order!!.contactPhone) }
    var status by remember { mutableStateOf(order!!.status) }
    var pidDesc by remember { mutableStateOf(order!!.pidDesc) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Edit Work Order", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = jobId, onValueChange = { jobId = it }, label = { Text("Job ID") })
        OutlinedTextField(value = serviceNumber, onValueChange = { serviceNumber = it }, label = { Text("Service Number") })
        OutlinedTextField(value = addressA, onValueChange = { addressA = it }, label = { Text("A-End Address") })
        OutlinedTextField(value = addressB, onValueChange = { addressB = it }, label = { Text("B-End Address") })
        OutlinedTextField(value = appointmentDate, onValueChange = { appointmentDate = it }, label = { Text("Date") })
        OutlinedTextField(value = appointmentTime, onValueChange = { appointmentTime = it }, label = { Text("Time") })
        OutlinedTextField(value = contactName, onValueChange = { contactName = it }, label = { Text("Contact Name") })
        OutlinedTextField(value = contactPhone, onValueChange = { contactPhone = it }, label = { Text("Phone") })
        OutlinedTextField(value = status, onValueChange = { status = it }, label = { Text("Status") })
        OutlinedTextField(value = pidDesc, onValueChange = { pidDesc = it }, label = { Text("PID Desc") })
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(onClick = {
                val updated = order!!.copy(
                    jobId = jobId, serviceNumber = serviceNumber, addressA = addressA, addressB = addressB,
                    appointmentDate = appointmentDate, appointmentTime = appointmentTime,
                    contactName = contactName, contactPhone = contactPhone, status = status, pidDesc = pidDesc
                )
                viewModel.updateOrder(updated)
                onBack()
            }) {
                Text("Save Changes")
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = {
                viewModel.deleteOrder(order!!)
                onBack()
            }) {
                Text("Delete")
            }
        }
    }
}
