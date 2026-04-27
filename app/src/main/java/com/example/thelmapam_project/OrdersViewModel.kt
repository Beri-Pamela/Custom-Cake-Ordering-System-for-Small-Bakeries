package com.example.thelmapam_project

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val repository: CakeRepository
) : ViewModel() {
    val orders: Flow<List<Order>> = repository.getOrders()
}
