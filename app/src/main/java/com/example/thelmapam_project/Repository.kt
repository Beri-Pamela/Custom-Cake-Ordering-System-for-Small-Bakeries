package com.example.thelmapam_project

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CakeRepository @Inject constructor(
    private val context: Context,
    private val orderDao: OrderDao,
    private val firestore: FirebaseFirestore
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val _templates = MutableStateFlow<List<CakeTemplate>>(emptyList())
    val templates = _templates.asStateFlow()

    private val _flavors = MutableStateFlow<List<Flavor>>(emptyList())
    val flavors = _flavors.asStateFlow()

    private val _decorations = MutableStateFlow<List<Decoration>>(emptyList())
    val decorations = _decorations.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        // We will do this via a suspend function properly called from the ViewModels, but for now we'll launch a minimal coroutine 
        // to migrate local files to Firebase or fetch them
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Fetch Templates
                val tSnapshot = firestore.collection("templates").get().await()
                if (tSnapshot.isEmpty) {
                    val templatesJson = context.assets.open("templates.json").bufferedReader().use { it.readText() }
                    val tList = json.decodeFromString<List<CakeTemplate>>(templatesJson)
                    _templates.value = tList
                    tList.forEach { firestore.collection("templates").document(it.id).set(it) }
                } else {
                    _templates.value = tSnapshot.documents.mapNotNull {
                        json.decodeFromString<CakeTemplate>(json.encodeToString(it.data ?: return@mapNotNull null))
                    }
                }

                // Fetch Flavors
                val fSnapshot = firestore.collection("flavors").get().await()
                if (fSnapshot.isEmpty) {
                    val flavorsJson = try {
                        context.assets.open("flavors.json").bufferedReader().use { it.readText() }
                    } catch (e: Exception) { "[]" }
                    val fList = json.decodeFromString<List<Flavor>>(flavorsJson)
                    _flavors.value = fList
                    fList.forEach { firestore.collection("flavors").document(it.id).set(it) }
                } else {
                    _flavors.value = fSnapshot.documents.mapNotNull { it.toObject(Flavor::class.java) }
                }

                // Fetch Decorations
                val dSnapshot = firestore.collection("decorations").get().await()
                if (dSnapshot.isEmpty) {
                    val decorationsJson = try {
                        context.assets.open("decorations.json").bufferedReader().use { it.readText() }
                    } catch (e: Exception) { "[]" }
                    val dList = json.decodeFromString<List<Decoration>>(decorationsJson)
                    _decorations.value = dList
                    dList.forEach { firestore.collection("decorations").document(it.id).set(it) }
                } else {
                    _decorations.value = dSnapshot.documents.mapNotNull { it.toObject(Decoration::class.java) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getOrders(): Flow<List<Order>> {
        return orderDao.getAllOrders().map { entities ->
            entities.map { entity ->
                Order(
                    orderId = entity.orderId,
                    cakeDesign = json.decodeFromString<CakeDesign>(entity.cakeDesignJson),
                    deliveryType = entity.deliveryType,
                    date = entity.date,
                    address = entity.address,
                    status = entity.status,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    suspend fun saveOrder(order: Order) {
        val entity = OrderEntity(
            orderId = order.orderId,
            cakeDesignJson = json.encodeToString<CakeDesign>(order.cakeDesign),
            deliveryType = order.deliveryType,
            date = order.date,
            address = order.address,
            status = order.status,
            createdAt = order.createdAt
        )
        // Save to Local DB
        orderDao.insertOrder(entity)
        // Push to Firebase
        try {
            firestore.collection("orders").document(order.orderId).set(order).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
