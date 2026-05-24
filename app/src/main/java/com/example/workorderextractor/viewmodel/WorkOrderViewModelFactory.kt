package com.example.workorderextractor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.workorderextractor.data.AppDatabase

class WorkOrderViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkOrderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkOrderViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
