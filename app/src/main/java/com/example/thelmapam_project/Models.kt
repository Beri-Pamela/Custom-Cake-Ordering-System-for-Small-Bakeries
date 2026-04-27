package com.example.thelmapam_project

import kotlinx.serialization.Serializable

@Serializable
data class CakeTemplate(
    val id: String,
    val name: String,
    val imageUrl: String,
    val basePrice: Int,
    val category: String,
    val defaultDesign: CakeDesign
)

@Serializable
data class CakeDesign(
    val shape: String = "Round",
    val size: String = "8\"",
    val layers: Int = 1,
    val flavor: Flavor = Flavor("default_vanilla", "Vanilla", "", 0),
    val decorations: List<Decoration> = emptyList(),
    val messageText: String = "",
    val textColor: String = "Black",
    val baseImageUri: String? = null,
    val totalPrice: Int = 15000,
    val deliveryLocation: String = "",
    val deliveryDate: Long = 0L
)

@Serializable
data class Flavor(
    val id: String,
    val name: String,
    val imageUrl: String,
    val priceExtra: Int,
    val isVegan: Boolean = false,
    val isGlutenFree: Boolean = false
)

@Serializable
data class Decoration(
    val id: String,
    val name: String,
    val imageUrl: String,
    val priceExtra: Int,
    val category: String,
    val positionX: Float = 0f,
    val positionY: Float = 0f
)

@Serializable
data class Order(
    val orderId: String,
    val cakeDesign: CakeDesign,
    val deliveryType: String,
    val date: Long,
    val address: String?,
    val status: String,
    val createdAt: Long
)
