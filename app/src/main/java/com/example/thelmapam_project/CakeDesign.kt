package com.example.thelmapam_project

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CustomizeViewModel @Inject constructor() : ViewModel() {

    private val _cakeDesign = MutableStateFlow(CakeDesign())
    val cakeDesign: StateFlow<CakeDesign> = _cakeDesign.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun updateShape(newShape: String) {
        _cakeDesign.update { it.copy(shape = newShape) }
        recalculatePrice()
    }

    fun updateLayers(layers: Int) {
        _cakeDesign.update { it.copy(layers = layers) }
        recalculatePrice()
    }

    fun updateSize(size: String) {
        _cakeDesign.update { it.copy(size = size) }
        recalculatePrice()
    }

    fun updateFlavor(flavor: Flavor) {
        _cakeDesign.update { it.copy(flavor = flavor) }
        recalculatePrice()
    }

    fun updateLocation(location: String) {
        _cakeDesign.update { it.copy(deliveryLocation = location) }
    }

    fun updateDate(date: Long) {
        _cakeDesign.update { it.copy(deliveryDate = date) }
    }

    fun addDecoration(decoration: Decoration) {
        _cakeDesign.update {
            it.copy(decorations = it.decorations + decoration)
        }
        recalculatePrice()
    }

    fun removeDecoration(decoration: Decoration) {
        _cakeDesign.update {
            it.copy(decorations = it.decorations.filter { d -> d.id != decoration.id })
        }
        recalculatePrice()
    }

    fun updateMessage(message: String) {
        _cakeDesign.update { it.copy(messageText = message) }
    }

    private fun recalculatePrice() {
        _cakeDesign.update { current ->
            val basePrice = 15000 // In Naira
            val flavorPrice = current.flavor.priceExtra
            val decorationPrice = current.decorations.sumOf { it.priceExtra }
            
            val layerPrice = (current.layers - 1) * 5000
            val sizeMultiplier = when(current.size) {
                "10\"" -> 2000
                "12\"" -> 4000
                else -> 0
            }

            current.copy(totalPrice = basePrice + flavorPrice + decorationPrice + layerPrice + sizeMultiplier)
        }
    }
}
