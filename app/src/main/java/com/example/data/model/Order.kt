package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val email: String,
    val phone: String = "",
    val address: String = "",
    val city: String = "",
    val totalOrders: Int = 0,
    val totalSpent: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "commandes")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val customerId: Long,
    val customerName: String,
    val customerEmail: String = "",
    val totalAmount: Double,
    val status: String, // "EN_ATTENTE", "PAYEE", "EXPEDIEE", "LIVREE", "ANNULEE"
    val paymentMethod: String = "Carte Bancaire",
    val shippingAddress: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "lignes_commande")
data class OrderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)
