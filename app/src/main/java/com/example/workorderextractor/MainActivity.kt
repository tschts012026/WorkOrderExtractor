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
import kotlinx.coroutines.flow.firstOrNull

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
        Text("輸入文章（如工單內容）", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = rawText,
            onValueChange = { rawText = it },
            modifier = Modifier.fillMaxWidth().height(200.dp),
            placeholder = { Text("請貼上或輸入包含工單資料的文章...") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (rawText.isNotBlank()) {
                extractedOrder = WorkOrderExtractor.extract(rawText)
                showPreview = true
            }
        }) {
            Text("提取資料")
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (showPreview && extractedOrder != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("提取結果預覽", style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()
                    Text("Job ID: ${extractedOrder!!.jobId}")
                    Text("Service No: ${extractedOrder!!.serviceNumber}")
                    Text("A端地址: ${extractedOrder!!.addressA}")
                    Text("B端地址: ${extractedOrder!!.addressB}")
                    Text("日期: ${extractedOrder!!.appointmentDate}")
                    Text("時間: ${extractedOrder!!.appointmentTime}")
                    Text("聯絡人: ${extractedOrder!!.contactName}")
                    Text("電話: ${extractedOrder!!.contactPhone}")
                    Text("Status: ${extractedOrder!!.status}")
                    Text("PID Desc: ${extractedOrder!!.pidDesc}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Button(onClick = {
                            viewModel.insertOrder(extractedOrder!!)
                            onNavigateToList()
                        }) {
                            Text("儲存")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { showPreview = false }) {
                            Text("取消")
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onNavigateToList) {
            Text("查看所有已儲存工單")
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
            Text("已儲存的工單", style = MaterialTheme.typography.titleLarge)
        }
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                viewModel.updateSearchQuery(it)
            },
            label = { Text("搜尋") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(orders) { order ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    onClick = { onEdit(order.id) }) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Job ID: ${order.jobId}", style = MaterialTheme.typography.titleMedium)
                        Text("日期: ${order.appointmentDate} ${order.appointmentTime}")
                        Text("聯絡人: ${order.contactName} (${order.contactPhone})")
                        Text("地址: ${order.addressA.take(20)}...")
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
        Text("編輯工單", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = jobId, onValueChange = { jobId = it }, label = { Text("Job ID") })
        OutlinedTextField(value = serviceNumber, onValueChange = { serviceNumber = it }, label = { Text("Service Number") })
        OutlinedTextField(value = addressA, onValueChange = { addressA = it }, label = { Text("A端地址") })
        OutlinedTextField(value = addressB, onValueChange = { addressB = it }, label = { Text("B端地址") })
        OutlinedTextField(value = appointmentDate, onValueChange = { appointmentDate = it }, label = { Text("日期") })
        OutlinedTextField(value = appointmentTime, onValueChange = { appointmentTime = it }, label = { Text("時間") })
        OutlinedTextField(value = contactName, onValueChange = { contactName = it }, label = { Text("聯絡人") })
        OutlinedTextField(value = contactPhone, onValueChange = { contactPhone = it }, label = { Text("電話") })
        OutlinedTextField(value = status, onValueChange = { status = it }, label = { Text("Status") })
        OutlinedTextField(value = pidDesc, onValueChange = { pidDesc = it }, label = { Text("PID Desc") })
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(onClick = {
                val updated = order!!.copy(
                    jobId = jobId,
                    serviceNumber = serviceNumber,
                    addressA = addressA,
                    addressB = addressB,
                    appointmentDate = appointmentDate,
                    appointmentTime = appointmentTime,
                    contactName = contactName,
                    contactPhone = contactPhone,
                    status = status,
                    pidDesc = pidDesc
                )
                viewModel.updateOrder(updated)
                onBack()
            }) {
                Text("儲存修改")
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = {
                viewModel.deleteOrder(order!!)
                onBack()
            }) {
                Text("刪除")
            }
        }
    }
}
