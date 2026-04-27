package com.example.thelmapam_project

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val cakeDesignJson: String,
    val deliveryType: String,
    val date: Long,
    val address: String?,
    val status: String,
    val createdAt: Long
)

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)
}

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromCakeDesign(value: CakeDesign): String = json.encodeToString(value)

    @TypeConverter
    fun toCakeDesign(value: String): CakeDesign = json.decodeFromString(value)
}

@Database(entities = [OrderEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
}
