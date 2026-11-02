package io.github.kiwiwhale.kiwiflow.example.common.dto

data class Order(
    val id: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Long
)

data class Payment(
    val orderId: String,
    val amount: Long
)

data class Shipment(
    val orderId: String,
    val trackingNumber: String
)

data class OrderResult(
    val orderId: String,
    val payment: Payment,
    val shipment: Shipment
)