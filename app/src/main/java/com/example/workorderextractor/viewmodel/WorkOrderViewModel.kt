package com.example.workorderextractor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.workorderextractor.data.AppDatabase
import com.example.workorderextractor.data.WorkOrder
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WorkOrderViewModel(private val db: AppDatabase) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val orders: StateFlow<List<WorkOrder>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) db.workOrderDao().getAllOrders()
        else db.workOrderDao().searchOrders(query)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun insertOrder(order: WorkOrder) {
        viewModelScope.launch { db.workOrderDao().insert(order) }
    }

    fun updateOrder(order: WorkOrder) {
        viewModelScope.launch { db.workOrderDao().update(order) }
    }

    fun deleteOrder(order: WorkOrder) {
        viewModelScope.launch { db.workOrderDao().delete(order) }
    }

    suspend fun getOrderById(id: Int): WorkOrder? {
        return db.workOrderDao().getAllOrders().firstOrNull()?.find { it.id == id }
    }
}
